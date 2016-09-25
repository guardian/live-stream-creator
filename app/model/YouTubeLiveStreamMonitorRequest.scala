package model

import play.api.libs.json._

case class YouTubeLiveStreamMonitorRequest(monitor: Boolean)

object YouTubeLiveStreamMonitorRequest {
  implicit val reads: Reads[YouTubeLiveStreamMonitorRequest] = Json.reads[YouTubeLiveStreamMonitorRequest]
  implicit val writes: Writes[YouTubeLiveStreamMonitorRequest] = Json.writes[YouTubeLiveStreamMonitorRequest]
}

case class YouTubeLiveStreamStartRequest(start: Boolean)

object YouTubeLiveStreamStartRequest {
  implicit val reads: Reads[YouTubeLiveStreamStartRequest] = Json.reads[YouTubeLiveStreamStartRequest]
  implicit val writes: Writes[YouTubeLiveStreamStartRequest] = Json.writes[YouTubeLiveStreamStartRequest]
}

case class YouTubeLiveStreamStopRequest(stop: Boolean)

object YouTubeLiveStreamStopRequest {
  implicit val reads: Reads[YouTubeLiveStreamStopRequest] = Json.reads[YouTubeLiveStreamStopRequest]
  implicit val writes: Writes[YouTubeLiveStreamStopRequest] = Json.writes[YouTubeLiveStreamStopRequest]
}
