package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class IncomingStream (
  name: String,
  source: String
)

object IncomingStream {
  implicit val incomingStreamReads: Reads[IncomingStream] = (
    (__ \ "name").read[String] ~
    (__ \ "sourceIp").read[String]
  )(IncomingStream.apply _)

  implicit val incomingStreamWrites: Writes[IncomingStream] = (
    (__ \ "name").write[String] ~
    (__ \ "sourceIp").write[String]
  )(unlift(IncomingStream.unapply))
}

case class OutgoingStream (
  entryName: String,
  sourceStreamName: String,
  streamName: String,
  host: String,
  application: String,
  enabled: Boolean,
  destinationName: Option[String],
  profile: String = "rtmp",
  port: Int = 1935
)

object OutgoingStream {
  implicit val outgoingStreamReads: Reads[OutgoingStream] = (
    (__ \ "entryName").read[String] ~
    (__ \ "sourceStreamName").read[String] ~
    (__ \ "streamName").read[String] ~
    (__ \ "host").read[String] ~
    (__ \ "application").read[String] ~
    (__ \ "enabled").read[Boolean] ~
    (__ \\ "destinationName").readNullable[String] ~
    (__ \ "profile").read[String] ~
    (__ \ "port").read[Int]
  )(OutgoingStream.apply _)

  implicit val outgoingStreamWrites: Writes[OutgoingStream] = (
    (__ \ "entryName").write[String] ~
    (__ \ "sourceStreamName").write[String] ~
    (__ \ "streamName").write[String] ~
    (__ \ "host").write[String] ~
    (__ \ "application").write[String] ~
    (__ \ "enabled").write[Boolean] ~
    (__ \ "extraOptions" \ "destinationName").writeNullable[String] ~
    (__ \ "profile").write[String] ~
    (__ \ "port").write[Int]
  )(unlift(OutgoingStream.unapply))

  def build (incomingStream: IncomingStream, youtubeLiveStream: YTLiveStream) = {
    OutgoingStream(
      youtubeLiveStream.title,
      incomingStream.name,
      youtubeLiveStream.streamName,
      youtubeLiveStream.host,
      youtubeLiveStream.applicationName,
      enabled = true,
      Some("youtube")
    )
  }
}
