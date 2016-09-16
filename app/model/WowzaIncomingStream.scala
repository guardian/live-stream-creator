package model

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, Writes, _}

case class WowzaIncomingStream(name: String, source: String)

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
