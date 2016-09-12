package lib

import java.net.URI

import play.api.libs.json.{JsValue, Json}

import scalaj.http._

case class HttpMethod(verb: String) {override def toString: String = verb}

object GET extends HttpMethod("GET")
object POST extends HttpMethod("POST")
object DELETE extends HttpMethod("DELETE")
object PUT extends HttpMethod("PUT")

object Request {
  private def response(httpRequest: HttpRequest) = {
    val response = httpRequest.execute()

    response.code match {
      case 200 => Some(Json.parse(response.body))
      case _ => None
    }
  }

  val applicationJson = "application/json"

  def get(path: URI): Option[JsValue] = {
    val request = Http(path.toString)
      .method(GET.toString)
      .header("Accept", applicationJson)
      .charset("utf-8")

    response(request)
  }

  def post(path: URI, data: JsValue): Option[JsValue] = {
    val request = Http(path.toString)
      .method(POST.toString)
      .header("Accept", applicationJson)
      .header("Content-type", applicationJson)
      .charset("utf-8")
      .postData(data.toString)

    response(request)
  }

  def delete(path: URI): Option[JsValue] = {
    val request = Http(path.toString)
      .method(DELETE.toString)
      .header("Accept", applicationJson)
      .charset("utf-8")

    response(request)
  }

  def put(path: URI, data: JsValue): Option[JsValue] = {
    val request = Http(path.toString)
      .method(PUT.toString)
      .header("Accept", applicationJson)
      .header("Content-type", applicationJson)
      .charset("utf-8")
      .put(data.toString)

    response(request)
  }
}
