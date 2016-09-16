package lib

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.{DateTime => GDateTime}
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model._
import lib.Config.{isContentOwnerMode, youtubeContentOwner}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object YouTubeDateTime {
  def now(): GDateTime = {
    // because the Google SDK has its own Date object ¯\_(ツ)_/¯
    new GDateTime(ISODateTimeFormat.dateTime.print(DateTime.now))
  }
}

case class TransitionState(status: String) { override def toString: String = status }
object TestingState extends TransitionState("testing")
object LiveState extends TransitionState("live")
object CompleteState extends TransitionState("complete")

trait YouTubeAuth {
  private val httpTransport = new NetHttpTransport()
  private val jacksonFactory = new JacksonFactory()

  private implicit val credentials: GoogleCredential = {
    new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(jacksonFactory)
      .setClientSecrets(Config.youtubeClientId, Config.youtubeClientSecret)
      .build
      .setRefreshToken(Config.youtubeRefreshToken)
  }

  implicit val youtube = {
    new YouTube.Builder(httpTransport, jacksonFactory, credentials)
      .setApplicationName(Config.youtubeAppName)
      .build
  }
}

object YouTubeChannelApi extends YouTubeAuth {
  def get(id: String): Future[Option[Channel]] = list(Some(id)).map(_.headOption)

  def list(id: Option[String] = None): Future[List[Channel]] = Future {
    val request = youtube.channels
      .list("id,snippet,contentOwnerDetails")
      .setMaxResults(50.toLong)

    if (id.isDefined) request.setId(id.get)

    if (isContentOwnerMode) {
      request.setManagedByMe(true)
        .setOnBehalfOfContentOwner(youtubeContentOwner.get)
    } else {
      if (id.isEmpty) request.setMine(true)
    }

    request.execute.getItems.asScala.toList
  }
}

object YouTubeStreamApi extends YouTubeAuth {
  def get(channel: Channel, id: String): Future[Option[LiveStream]] = list(channel, Some(id)).map(_.headOption)

  def isActive(stream: LiveStream): Boolean = stream.getStatus.getStreamStatus == "active"

  def list(channel: Channel, id: Option[String] = None) = Future {
    val request = youtube.liveStreams
      .list("id,cdn,snippet,status")

    if (id.isDefined) request.setId(id.get)

    if (isContentOwnerMode) {
      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channel.getId)
    } else {
      if (id.isEmpty) request.setMine(true)
    }

    request.execute.getItems.asScala.toList
  }

  def create(broadcast: LiveBroadcast): Future[LiveStream] = {
    createStream(broadcast).map(stream => {
      bindBroadcastToStream(broadcast, stream)
    })
  }

  private def bindBroadcastToStream(broadcast: LiveBroadcast, stream: LiveStream) = {
    val request = youtube.liveBroadcasts
      .bind(broadcast.getId, "id,contentDetails")
      .setStreamId(stream.getId)

    if (isContentOwnerMode) {
      val channelId = broadcast.getSnippet.getChannelId

      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channelId)
    }

    request.execute

    stream
  }

  private def createStream(broadcast: LiveBroadcast): Future[LiveStream] = Future {
    val title = broadcast.getSnippet.getTitle

    val snippet = new LiveStreamSnippet()
      .setTitle(title)

    val cdnSettings = new CdnSettings()
      .setFormat("720p")
      .setIngestionType("rtmp")

    val stream = new LiveStream()
      .setKind("youtube#liveStream")
      .setSnippet(snippet)
      .setCdn(cdnSettings)

    val request = youtube.liveStreams()
      .insert("cdn,snippet", stream)

    if (isContentOwnerMode) {
      val channelId = broadcast.getSnippet.getChannelId
      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channelId)
    }

    request.execute()
  }
}

object YouTubeBroadcastApi extends YouTubeAuth {
  def get(channel: Channel, id: String): Future[Option[LiveBroadcast]] = list(channel, Some(id)).map(_.headOption)

  def list(channel: Channel, id: Option[String] = None) = Future {
    val request = youtube.liveBroadcasts
      .list("id,snippet,contentDetails,status")

    if (id.isDefined) request.setId(id.get)

    if (isContentOwnerMode) {
      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channel.getId)
    } else {
      if (id.isEmpty) request.setMine(true)
    }

    request.execute.getItems.asScala.toList
  }

  def startMonitor(channel: Channel, broadcast: LiveBroadcast) = {
    transition(channel, broadcast, TestingState)
  }

  def start(channel: Channel, broadcast: LiveBroadcast) = {
    transition(channel, broadcast, LiveState)
  }

  def stop(channel: Channel, broadcast: LiveBroadcast) = {
    transition(channel, broadcast, CompleteState)
  }

  private def transition(channel: Channel, broadcast: LiveBroadcast, lifeCycleStatus: TransitionState): Future[Option[LiveBroadcast]] = {

    YouTubeStreamApi.get(channel, broadcast.getContentDetails.getBoundStreamId).map {
      case Some(stream) => {
        if (! YouTubeStreamApi.isActive(stream)) {
          // stream bound to broadcast has to be active before transitioning broadcast status
          // see https://developers.google.com/youtube/v3/live/docs/liveBroadcasts/transition
          None
        } else {
          val request = youtube.liveBroadcasts
            .transition(lifeCycleStatus.toString, broadcast.getId, "status")

          if (isContentOwnerMode) {
            request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
              .setOnBehalfOfContentOwnerChannel(stream.getSnippet.getChannelId)
          }

          Some(request.execute)
        }
      }
      case _ => None
    }
  }

  def create(channel: Channel, title: String) = Future {
    val snippet = new LiveBroadcastSnippet()
      .setTitle(title)
      .setScheduledStartTime(YouTubeDateTime.now())
      .setChannelId(channel.getId)

    val status = new LiveBroadcastStatus()
      .setPrivacyStatus("public")

    val broadcast = new LiveBroadcast()
      .setKind("youtube#liveBroadcast")
      .setSnippet(snippet)
      .setStatus(status)

    val request = youtube.liveBroadcasts()
      .insert("snippet,status,contentDetails", broadcast)

    if (isContentOwnerMode) {
      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channel.getId)
    }

    request.execute
  }
}
