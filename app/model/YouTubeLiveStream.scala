package model

import com.google.api.services.youtube.model.{Channel, LiveBroadcast, LiveStream}
import play.api.libs.json.{Json, Reads, Writes}

case class YouTubeLiveStream (
  id: String,
  broadcastId: String,
  channelId: String,
  videoId: String
)

object YouTubeLiveStream {
  implicit val reads: Reads[YouTubeLiveStream] = Json.reads[YouTubeLiveStream]
  implicit val writes: Writes[YouTubeLiveStream] = Json.writes[YouTubeLiveStream]

  def build(channel: Channel, stream: LiveStream, broadcast: LiveBroadcast): YouTubeLiveStream = {
    val id = stream.getId
    val broadcastId = broadcast.getId
    val channelId = channel.getId
    val videoId = broadcast.getId

    YouTubeLiveStream(id, broadcastId, channelId, videoId)
  }
}

case class YouTubeStreamHealthStatus (status: String) {
  override def toString = status
}
object HealthActive extends YouTubeStreamHealthStatus("active")

object YouTubeStreamHealthStatus {
  implicit val reads: Reads[YouTubeStreamHealthStatus] = Json.reads[YouTubeStreamHealthStatus]
  implicit val writes: Writes[YouTubeStreamHealthStatus] = Json.writes[YouTubeStreamHealthStatus]

  def build(status: String): YouTubeStreamHealthStatus = {
    status match {
      case "active" => HealthActive
      case _ => YouTubeStreamHealthStatus(status)
    }
  }
}

