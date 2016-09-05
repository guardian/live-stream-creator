package lib

import play.api.libs.json._
import scalaj.http._
import play.api.libs.functional.syntax._

case class WowzaApplication (
  id: String,
  href: String,
  appType: String,
  dvrEnabled: Boolean,
  transcoderEnabled: Boolean,
  streamTargetsEnabled: Boolean
)

object WowzaApplication {
  def build(js: JsValue) = {
    
  }

  implicit val jsonReads: Reads[WowzaApplication] = Json.reads[WowzaApplication]
  implicit val jsonWrites: Writes[WowzaApplication] = Json.writes[WowzaApplication]
}


object Wowza {
  def buildRequest (path: String) = {
    Http(s"${Config.wowzaHost}:${Config.wowzaPort}/$path")
      .header("Accept", "application/json")
      .charset("utf-8")
  }

  def response (path: String) = {
    val response = buildRequest(path).execute()
    response.code match {
      case 200 => {
        val x: JsValue = Json.parse(response.body)

      }
      case _ => None
    }
  }

  def getApplications() = {

  }
}
