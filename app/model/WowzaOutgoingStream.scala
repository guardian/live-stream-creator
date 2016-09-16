package model

import java.net.URI

import com.google.api.services.youtube.model.LiveStream
import play.api.libs.functional.syntax._
import play.api.libs.json._

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

  def build (incomingStream: WowzaIncomingStream, liveStream: LiveStream, enabled: Boolean) = {
    val title = liveStream.getSnippet.getTitle
    val ingestionInfo = liveStream.getCdn.getIngestionInfo
    val ingestionAddress = new URI(ingestionInfo.getIngestionAddress)

    WowzaOutgoingStream(
      title,
      incomingStream.name,
      ingestionInfo.getStreamName,
      ingestionAddress.getHost,
      ingestionAddress.getPath,
      enabled,
      Some("youtube")
    )
  }
}
