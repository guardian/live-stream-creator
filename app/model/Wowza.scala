package model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WowzaIncomingStream(
  name: String,
  source: String
)

object WowzaIncomingStream {
  implicit val incomingStreamReads: Reads[WowzaIncomingStream] = (
    (__ \ "name").read[String] ~
    (__ \ "sourceIp").read[String]
  )(WowzaIncomingStream.apply _)

  implicit val incomingStreamWrites: Writes[WowzaIncomingStream] = (
    (__ \ "name").write[String] ~
    (__ \ "sourceIp").write[String]
  )(unlift(WowzaIncomingStream.unapply))
}

case class WowzaOutgoingStream(
  entryName: String,
  sourceStreamName: String,
  streamName: String,
  host: String,
  application: String,
  enabled: Boolean,
  destinationName: Option[String],
  profile: String = "rtmp",
  port: Int = 1935
) {
  def safeEntryName = entryName.replace(" ", "")

  def toggleEnabled(newEnabledState: Boolean) = {
    WowzaOutgoingStream (
      entryName,
      sourceStreamName,
      streamName,
      host,
      application,
      newEnabledState,
      destinationName,
      profile,
      port
    )
  }
}

object WowzaOutgoingStream {
  implicit val outgoingStreamReads: Reads[WowzaOutgoingStream] = (
    (__ \ "entryName").read[String] ~
    (__ \ "sourceStreamName").read[String] ~
    (__ \ "streamName").read[String] ~
    (__ \ "host").read[String] ~
    (__ \ "application").read[String] ~
    (__ \ "enabled").read[Boolean] ~
    (__ \\ "destinationName").readNullable[String] ~
    (__ \ "profile").read[String] ~
    (__ \ "port").read[Int]
  )(WowzaOutgoingStream.apply _)

  implicit val outgoingStreamWrites: Writes[WowzaOutgoingStream] = (
    (__ \ "entryName").write[String] ~
    (__ \ "sourceStreamName").write[String] ~
    (__ \ "streamName").write[String] ~
    (__ \ "host").write[String] ~
    (__ \ "application").write[String] ~
    (__ \ "enabled").write[Boolean] ~
    (__ \ "extraOptions" \ "destinationName").writeNullable[String] ~
    (__ \ "profile").write[String] ~
    (__ \ "port").write[Int]
  )(unlift(WowzaOutgoingStream.unapply))

  def build (incomingStream: WowzaIncomingStream, youtubeLiveStream: YouTubeLiveStream, enabled: Boolean) = {
    WowzaOutgoingStream(
      youtubeLiveStream.title,
      incomingStream.name,
      youtubeLiveStream.streamName,
      youtubeLiveStream.host,
      youtubeLiveStream.applicationName,
      enabled,
      Some("youtube")
    )
  }
}
