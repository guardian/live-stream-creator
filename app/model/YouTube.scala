package model

import java.net.URI

import com.google.api.services.youtube.model.{Channel, LiveStream}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.Try

case class YouTubeLiveStreamRequest(
  title: String,
  channel: String,
  wowzaApp: String,
  wowzaStream: String
)

object YouTubeLiveStreamRequest {
  implicit val reads: Reads[YouTubeLiveStreamRequest] = Json.reads[YouTubeLiveStreamRequest]
  implicit val writes: Writes[YouTubeLiveStreamRequest] = Json.writes[YouTubeLiveStreamRequest]
}

case class YouTubeLiveStream(
  streamName: String,
  host: String,
  applicationName: String,
  title: String
)

object YouTubeLiveStream {
  implicit val reads: Reads[YouTubeLiveStream] = Json.reads[YouTubeLiveStream]
  implicit val writes: Writes[YouTubeLiveStream] = Json.writes[YouTubeLiveStream]

  def build(stream: LiveStream) = {
    val ingestionInfo = stream.getCdn.getIngestionInfo
    val ingestionAddress = new URI(ingestionInfo.getIngestionAddress)

    YouTubeLiveStream(
      ingestionInfo.getStreamName,
      ingestionAddress.getHost,
      ingestionAddress.getPath,
      stream.getSnippet.getTitle
    )
  }
}

case class YouTubeChannel(
  id: String,
  title: String,
  thumbnail: URI,
  contentOwner: Option[String]
)

object YouTubeChannel {
  implicit val reads: Reads[YouTubeChannel] = (
    (__ \ "id").read[String] ~
    (__ \ "title").read[String] ~
    (__ \ "thumbnail").read[String].map(URI.create) ~
    (__ \ "contentOwner").readNullable[String]
  )(YouTubeChannel.apply _)

  implicit val writes: Writes[YouTubeChannel] = (
    (__ \ "id").write[String] ~
    (__ \ "title").write[String] ~
    (__ \ "thumbnail").write[String].contramap((_: URI).toString) ~
    (__ \ "contentOwner").writeNullable[String]
  )(unlift(YouTubeChannel.unapply))

  def build(channel: Channel) = {
    YouTubeChannel(
      channel.getId,
      channel.getSnippet.getTitle,
      URI.create(channel.getSnippet.getThumbnails.getDefault.getUrl),
      Try { channel.getContentOwnerDetails.getContentOwner }.toOption
    )
  }
}
