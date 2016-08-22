package utils

import play.api.libs.json.{JsResultException, Json, JsValue}

import scala.io.Source

class InvalidCredentialException extends Exception {
}

case class credentials(filename:String) {
  def read(): Map[String,String] = {
    val source: String = Source.fromFile(filename).getLines().mkString
    val jsdata = Json.parse(source)

    try {
      Map(
        "client_id" -> (jsdata \ "web" \ "client_id").as[String],
        "project_id" -> (jsdata \ "web" \ "project_id").as[String],
        "auth_uri" -> (jsdata \ "web" \ "auth_uri").as[String],
        "token_uri" -> (jsdata \ "web" \ "token_uri").as[String],
        "auth_provider_x509_cert_url" -> (jsdata \ "web" \ "auth_provider_x509_cert_url").as[String],
        "client_secret" -> (jsdata \ "web" \ "client_secret").as[String]
      )
    } catch {
      case e:JsResultException =>
        println("Invalid credentials!")
        throw new InvalidCredentialException
    }
  }
}
