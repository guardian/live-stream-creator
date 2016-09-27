package model

import com.google.api.services.youtube.model.{Channel, LiveBroadcast, LiveStream}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class YouTubeLiveStream (
  id: String,
  broadcastId: String,
  channel: YouTubeChannel,
  videoId: String,
  wowzaApp: String,
  wowzaOutgoingStream: WowzaOutgoingStream
)

object YouTubeLiveStream {
  implicit val reads: Reads[YouTubeLiveStream] = (
    (__ \ "id").read[String] ~
    (__ \ "broadcastId").read[String] ~
    (__ \ "channel").read[YouTubeChannel] ~
    (__ \ "video").read[String] ~
    (__ \ "wowzaApp").read[String] ~
    (__ \ "wowzaOutgoingStream").read[WowzaOutgoingStream]
  )(YouTubeLiveStream.apply _)

  implicit val writes: Writes[YouTubeLiveStream] = (
    (__ \ "id").write[String] ~
    (__ \ "broadcastId").write[String] ~
    (__ \ "channel").write[YouTubeChannel] ~
    (__ \ "videoId").write[String] ~
    (__ \ "wowzaApp").write[String] ~
    (__ \ "wowzaOutgoingStream").write[WowzaOutgoingStream]
  )(unlift(YouTubeLiveStream.unapply))

  def build(ytChannel: Channel, stream: LiveStream, broadcast: LiveBroadcast, wowzaApp: String, wowzaOutgoingStream: WowzaOutgoingStream): YouTubeLiveStream = {
    val id = stream.getId
    val broadcastId = broadcast.getId
    val channel = YouTubeChannel.build(ytChannel)
    val videoId = broadcast.getId

    YouTubeLiveStream(id, broadcastId, channel, videoId, wowzaApp, wowzaOutgoingStream)
  }
}

case class YouTubeStreamHealthStatus (
  streamStatus: String,
  broadcastStatus: String
)

object YouTubeStreamHealthStatus {
  implicit val reads: Reads[YouTubeStreamHealthStatus] = Json.reads[YouTubeStreamHealthStatus]
  implicit val writes: Writes[YouTubeStreamHealthStatus] = Json.writes[YouTubeStreamHealthStatus]
}

