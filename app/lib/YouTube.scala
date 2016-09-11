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

object YouTubeChannel extends YouTubeAuth {
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

object YouTubeStream extends YouTubeAuth {
  def get(channel: Channel, id: String): Option[LiveStream] = list(channel, Some(id)).headOption

  def isActive(stream: LiveStream): Boolean = stream.getStatus.getStreamStatus == "active"

  def list(channel: Channel, id: Option[String] = None) = {
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

  def create(broadcast: LiveBroadcast) = {
    val stream = createStream(broadcast)

    val request = youtube.liveBroadcasts
      .bind(broadcast.getId, "id,contentDetails")
      .setStreamId(stream.getId)

    if (isContentOwnerMode) {
      val channelId = broadcast.getSnippet.getChannelId

      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channelId)
    }

    request.execute
  }

  private def createStream(broadcast: LiveBroadcast): LiveStream = {
    val title = broadcast.getSnippet.getTitle
    val channelId = broadcast.getSnippet.getChannelId

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
      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channelId)
    }

    request.execute()
  }
}

case class StreamPrivacy (status: String) {
  override def toString: String = status
}

object PrivateStreamVisibility extends StreamPrivacy("private")

object PublicStreamVisibility extends StreamPrivacy("public")

object UnlistedStreamVisibility extends StreamPrivacy("unlisted")

case class LifeCycleStatus (status: String) {
  override def toString: String = status
}

object TestingBroadcastStatus extends LifeCycleStatus("testing")

object LiveBroadcastStatus extends LifeCycleStatus("live")

object CompleteBroadcastStatus extends LifeCycleStatus("complete")

object YouTubeBroadcast extends YouTubeAuth {
  def get(channel: Channel, id: String): Option[LiveBroadcast] = list(channel, Some(id)).headOption

  def list(channel: Channel, id: Option[String] = None) = {
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
    updateStatus(channel, broadcast, PrivateStreamVisibility, TestingBroadcastStatus)
  }

  def start(channel: Channel, broadcast: LiveBroadcast) = {
    updateStatus(channel, broadcast, PublicStreamVisibility, LiveBroadcastStatus)
  }

  def stop(channel: Channel, broadcast: LiveBroadcast) = {
    updateStatus(channel, broadcast, UnlistedStreamVisibility, CompleteBroadcastStatus)
  }

  private def updateStatus(channel: Channel, broadcast: LiveBroadcast, streamPrivacy: StreamPrivacy, lifeCycleStatus: LifeCycleStatus): Option[LiveBroadcast] = {
    val stream = YouTubeStream.get(channel, broadcast.getContentDetails.getBoundStreamId).get

    if (! YouTubeStream.isActive(stream)) {
      // stream bound to broadcast has to be active before transitioning broadcast status
      // see https://developers.google.com/youtube/v3/live/docs/liveBroadcasts/transition
      None
    } else {
      val status = new LiveBroadcastStatus()
        .setPrivacyStatus(streamPrivacy.toString)
        .setLifeCycleStatus(lifeCycleStatus.toString)

      broadcast.setStatus(status)

      val request = youtube.liveBroadcasts.update("status", broadcast)

      if (isContentOwnerMode) {
        request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
          .setOnBehalfOfContentOwnerChannel(stream.getSnippet.getChannelId)
      }

      Some(request.execute)
    }
  }

  def create(channel: Channel, title: String) = {
    val snippet = new LiveBroadcastSnippet()
      .setTitle(title)
      .setScheduledStartTime(YouTubeDateTime.now())
      .setChannelId(channel.getId)

    val status = new LiveBroadcastStatus()
      .setPrivacyStatus(PrivateStreamVisibility.toString) // make private by default

    val monitorStreamInfo = new MonitorStreamInfo()
      .setEnableMonitorStream(true)

    val contentDetails = new LiveBroadcastContentDetails()
      .setMonitorStream(monitorStreamInfo)

    val broadcast = new LiveBroadcast()
      .setKind("youtube#liveBroadcast")
      .setSnippet(snippet)
      .setStatus(status)
      .setContentDetails(contentDetails)

    val request = youtube.liveBroadcasts()
      .insert("snippet,status,contentDetails", broadcast)

    if (isContentOwnerMode) {
      request.setOnBehalfOfContentOwner(youtubeContentOwner.get)
        .setOnBehalfOfContentOwnerChannel(channel.getId)
    }

    request.execute
  }
}
