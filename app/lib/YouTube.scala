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

case class StreamState(status: String) { override def toString: String = status }
object TestingState extends StreamState("testing")
object LiveState extends StreamState("live")
object CompleteState extends StreamState("complete")
object ReadyState extends StreamState("ready")

case class Visibility(status: String) { override def toString: String = status }
object PublicVisibility extends Visibility("public")
object UnlistedVisibility extends Visibility("unlisted")
object PrivateVisibility extends Visibility("private")

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
      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
      if (id.isEmpty) request.setManagedByMe(true)
    } else {
      if (id.isEmpty) request.setMine(true)
    }

    request.execute.getItems.asScala.toList
  }
}

object YouTubeStreamApi extends YouTubeAuth {
  def get(channel: Channel, id: String): Future[Option[LiveStream]] = list(channel, Some(id)).map(_.headOption)

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

  def getStatus(stream: LiveStream) = {
    stream.getStatus.getStreamStatus
  }

  def isActive(stream: LiveStream): Boolean = getStatus(stream) == "active"

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

  def monitor(channel: Channel, broadcast: LiveBroadcast) = {
    transition(channel, broadcast, TestingState)
  }

  def start(channel: Channel, broadcast: LiveBroadcast) = {
    transition(channel, broadcast, LiveState)
  }

  def stop(channel: Channel, broadcast: LiveBroadcast) = {
    transition(channel, broadcast, CompleteState)
  }

  def getStatus(broadcast: LiveBroadcast): StreamState = {
    broadcast.getStatus.getLifeCycleStatus match {
      case "ready" => ReadyState
      case "testing" => TestingState
      case "live" => LiveState
      case "complete" => CompleteState
      case status => StreamState(status)
    }
  }

  def isValidTransition(stream: LiveStream, broadcast: LiveBroadcast, newState: StreamState): Boolean = {
    val currentState = YouTubeBroadcastApi.getStatus(broadcast)

    currentState match {
      case ReadyState => {
        newState match {
          case TestingState => {
            // stream bound to broadcast has to be active before transitioning broadcast status
            // see https://developers.google.com/youtube/v3/live/docs/liveBroadcasts/transition
            YouTubeStreamApi.isActive(stream)
          }
          case LiveState => true
          case _ => false
        }
      }
      case TestingState => {
        newState match {
          case LiveState => true
          case _ => false
        }
      }
      case LiveState => {
        newState match {
          case CompleteState => true
          case _ => false
        }
      }
      case CompleteState => {
        newState match {
          case LiveState => true
          case _ => false
        }
      }
    }
  }

  private def transition(channel: Channel, broadcast: LiveBroadcast, lifeCycleStatus: StreamState): Future[Option[LiveBroadcast]] = {

    YouTubeStreamApi.get(channel, broadcast.getContentDetails.getBoundStreamId).map {
      case Some(stream) => {
        if (! YouTubeBroadcastApi.isValidTransition(stream, broadcast, lifeCycleStatus)) {
          None
        }
        else {
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
      .setPrivacyStatus(UnlistedVisibility.toString)

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
