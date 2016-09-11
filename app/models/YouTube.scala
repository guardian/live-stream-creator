package models

import java.net.URI

import com.google.api.services.youtube.model.{Channel, LiveStream}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.Try

case class YTLiveStream (
  streamName: String,
  host: String,
  applicationName: String,
  title: String
)

object YTLiveStream {
  def build(stream: LiveStream) = {
    val ingestionInfo = stream.getCdn.getIngestionInfo
    val ingestionAddress = new URI(ingestionInfo.getIngestionAddress)
    YTLiveStream(ingestionInfo.getStreamName, ingestionAddress.getHost, ingestionAddress.getPath, stream.getSnippet.getTitle)
  }
}

case class YTChannel (
  id: String,
  title: String,
  thumbnail: URI,
  contentOwner: Option[String]
)

object YTChannel {
  implicit val reads: Reads[YTChannel] = (
    (__ \ "id").read[String] ~
    (__ \ "title").read[String] ~
    (__ \ "thumbnail").read[String].map(URI.create) ~
    (__ \ "contentOwner").readNullable[String]
  )(YTChannel.apply _)

  implicit val writes: Writes[YTChannel] = (
    (__ \ "id").write[String] ~
    (__ \ "title").write[String] ~
    (__ \ "thumbnail").write[String].contramap((_: URI).toString) ~
    (__ \ "contentOwner").writeNullable[String]
  )(unlift(YTChannel.unapply))

  def build(channel: Channel) = {
    YTChannel(
      channel.getId,
      channel.getSnippet.getTitle,
      URI.create(channel.getSnippet.getThumbnails.getDefault.getUrl),
      Try { channel.getContentOwnerDetails.getContentOwner }.toOption
    )
  }
}
