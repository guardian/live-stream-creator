package model

import java.net.URI

import com.google.api.services.youtube.model.Channel
import com.gu.scanamo.DynamoFormat
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.Try

case class YouTubeChannel(
  id: String,
  title: String,
  thumbnail: URI,
  contentOwner: Option[String]
) {
  override def toString: String = Json.toJson(this).toString()
}

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

  private def build(json: String): YouTubeChannel = {
    Json.parse(json).as[YouTubeChannel]
  }

  implicit val stringFormat = DynamoFormat.coercedXmap[YouTubeChannel, String, IllegalArgumentException](YouTubeChannel.build)(_.toString)
}
