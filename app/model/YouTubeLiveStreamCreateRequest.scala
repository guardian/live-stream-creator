package model

import play.api.libs.json.{Json, Reads, Writes}

case class YouTubeLiveStreamCreateRequest(
  title: String,
  channel: String,
  wowzaApp: String,
  wowzaStream: String
)

object YouTubeLiveStreamCreateRequest {
  implicit val reads: Reads[YouTubeLiveStreamCreateRequest] = Json.reads[YouTubeLiveStreamCreateRequest]
  implicit val writes: Writes[YouTubeLiveStreamCreateRequest] = Json.writes[YouTubeLiveStreamCreateRequest]
}
