package model

import play.api.libs.json._

case class YouTubeLiveStreamUpdateRequest(monitor: Boolean)

object YouTubeLiveStreamUpdateRequest {
  implicit val reads: Reads[YouTubeLiveStreamUpdateRequest] = Json.reads[YouTubeLiveStreamUpdateRequest]
  implicit val writes: Writes[YouTubeLiveStreamUpdateRequest] = Json.writes[YouTubeLiveStreamUpdateRequest]
}
