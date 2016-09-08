package lib

import java.net.URI

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.{DateTime => GDateTime}
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.collection.JavaConverters._

object YouTubeDateTime {
  def now(): GDateTime = {
    // because the Google SDK has its own Date object ¯\_(ツ)_/¯
    new GDateTime(ISODateTimeFormat.dateTime.print(DateTime.now))
  }
}

trait YouTubeAuthenticatedApi {
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
      .setApplicationName("live-stream-creator")
      .build
  }
}

case class YouTubeChannel (id: String, title: String, isContentOwnerChannel: Boolean)

object YouTubeChannel {
  def build(channel: Channel, isContentOwnerChannel: Boolean) = {
    YouTubeChannel(channel.getId, channel.getSnippet.getTitle, isContentOwnerChannel)
  }
}

object YouTubeChannelApi extends YouTubeAuthenticatedApi {
  def list(contentOwner: Option[String] = None): List[YouTubeChannel] = {
    val request = youtube.channels.list("snippet").setMaxResults(50.toLong)

    contentOwner match {
      case Some(co) => request.setManagedByMe(true).setOnBehalfOfContentOwner(co)
      case None     => request.setMine(true)
    }

    request.execute.getItems.asScala.toList.map(YouTubeChannel.build(_, contentOwner.isDefined))
  }
}

case class BasicLiveStream (streamName: String, host: String, applicationName: String)

object BasicLiveStream  {
  def build(stream: LiveStream) = {
    val ingestionInfo = stream.getCdn.getIngestionInfo
    val ingestionAddress = new URI(ingestionInfo.getIngestionAddress)
    BasicLiveStream (ingestionInfo.getStreamName, ingestionAddress.getHost, ingestionAddress.getPath)
  }
}

object YouTubeStreamApi extends YouTubeAuthenticatedApi {
  def get(id: String): Option[LiveStream] = {
    val request = youtube.liveStreams
      .list("id,cdn,snippet,status")
      .setId(id)

    request.execute.getItems.asScala.toList.headOption
  }

  def isActive(id: String): Boolean = {
    get(id) match {
      case Some(stream) => stream.getStatus.getStreamStatus == "active"
      case _ => false
    }
  }

  def list() = {
    val request = youtube.liveStreams
      .list("id,cdn,snippet,status")
      .setMine(true)

    request.execute.getItems.asScala.toList
  }

  def create(channel: YouTubeChannel, title: String) = {
    val broadcast = YouTubeBroadcastApi.create(channel, title)
    val stream = createStream(title)

    youtube.liveBroadcasts
      .bind(broadcast.getId, "id,contentDetails")
      .setStreamId(stream.getId)
      .execute
  }

  private def createStream(title: String): LiveStream = {
    val snippet = new LiveStreamSnippet()
      .setTitle(title)

    val cdnSettings = new CdnSettings()
      .setFormat("720p")
      .setIngestionType("rtmp")

    val stream = new LiveStream()
      .setKind("youtube#liveStream")
      .setSnippet(snippet)
      .setCdn(cdnSettings)

    youtube.liveStreams()
      .insert("cdn,snippet", stream)
      .execute()
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

object YouTubeBroadcastApi extends YouTubeAuthenticatedApi {
  def get(id: String): Option[LiveBroadcast] = {
    val request = youtube.liveBroadcasts
      .list("id,snippet,contentDetails,status")
      .setId(id)

    request.execute.getItems.asScala.toList.headOption
  }

  def list() = {
    val request = youtube.liveBroadcasts
      .list("id,snippet,contentDetails,status")
      .setMine(true)

    request.execute.getItems.asScala.toList
  }

  def start(broadcast: LiveBroadcast) = {
    updateStatus(broadcast, PublicStreamVisibility, LiveBroadcastStatus)
  }

  def stop(broadcast: LiveBroadcast) = {
    updateStatus(broadcast, UnlistedStreamVisibility, CompleteBroadcastStatus)
  }

  private def updateStatus(broadcast: LiveBroadcast, streamPrivacy: StreamPrivacy, lifeCycleStatus: LifeCycleStatus): Option[LiveBroadcast] = {
    YouTubeStreamApi.isActive(broadcast.getContentDetails.getBoundStreamId) match {
      case true => {
        val status = new LiveBroadcastStatus()
          .setPrivacyStatus(streamPrivacy.toString)
          .setLifeCycleStatus(lifeCycleStatus.toString)

        broadcast.setStatus(status)

        val request = youtube.liveBroadcasts.update("status", broadcast)

        Some(request.execute)
      }
      case false => None
    }
  }

  def create(channel: YouTubeChannel, title: String) = {
    val snippet = new LiveBroadcastSnippet()
      .setTitle(title)
      .setScheduledStartTime(YouTubeDateTime.now())
      .setChannelId(channel.id)

    val status = new LiveBroadcastStatus()
      .setPrivacyStatus(PrivateStreamVisibility.toString) // make stream private by default

    val contentDetails = new LiveBroadcastContentDetails()
      .setEnableEmbed(true)

    val broadcast = new LiveBroadcast()
      .setKind("youtube#liveBroadcast")
      .setSnippet(snippet)
      .setStatus(status)
      .setContentDetails(contentDetails)

    youtube.liveBroadcasts()
      .insert("snippet,status", broadcast)
      .execute()
  }
}
