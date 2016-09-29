package model

import java.net.URI

import lib.Config
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, Writes, _}

case class WowzaRawIncomingStream(applicationInstance: String, name: String, source: String)

object WowzaRawIncomingStream {
  implicit val incomingStreamReads: Reads[WowzaRawIncomingStream] = (
    (__ \ "applicationInstance").read[String] ~
    (__ \ "name").read[String] ~
    (__ \ "sourceIp").read[String]
    )(WowzaRawIncomingStream.apply _)

  implicit val incomingStreamWrites: Writes[WowzaRawIncomingStream] = (
    (__ \ "applicationInstance").write[String] ~
    (__ \ "name").write[String] ~
    (__ \ "sourceIp").write[String]
    )(unlift(WowzaRawIncomingStream.unapply))
}


case class WowzaIncomingStream (
  application: String,
  applicationInstance: String,
  name: String,
  source: String,
  dashManifestUri: URI
)

object WowzaIncomingStream {
  implicit val reads: Reads[WowzaIncomingStream] = (
    (__ \ "application").read[String] ~
    (__ \ "applicationInstance").read[String] ~
    (__ \ "name").read[String] ~
    (__ \ "source").read[String] ~
    (__ \ "dashManifestUri").read[String].map(URI.create)
  )(WowzaIncomingStream.apply _)

  implicit val writes: Writes[WowzaIncomingStream] = (
    (__ \ "application").write[String] ~
    (__ \ "applicationInstance").write[String] ~
    (__ \ "name").write[String] ~
    (__ \ "source").write[String] ~
    (__ \ "dashManifestUri").write[String].contramap((_: URI).toString)
  )(unlift(WowzaIncomingStream.unapply))

  def build(app: String, rawIncoming: WowzaRawIncomingStream): WowzaIncomingStream = {
    val dashUri = URI.create(s"${Config.wowzaEndpoint}:${Config.wowzaStreamingPort}/$app/${rawIncoming.applicationInstance}/${rawIncoming.name}/manifest.mpd")

    WowzaIncomingStream(
      app,
      rawIncoming.applicationInstance,
      rawIncoming.name,
      rawIncoming.source,
      dashUri
    )
  }
}
