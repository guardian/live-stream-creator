package lib

import java.net.URI

import models._
import play.api.libs.json._

import scala.concurrent.Future
import scalaj.http._
import scala.concurrent.ExecutionContext.Implicits.global

object Request {
  private def response(httpRequest: HttpRequest) = {
    val response = httpRequest.execute()

    response.code match {
      case 200 => Some(Json.parse(response.body))
      case _ => None
    }
  }

  def get(path: URI): Option[JsValue] = {
    val request = Http(path.toString)
      .method(GET.toString)
      .header("Accept", "application/json")
      .charset("utf-8")

    response(request)
  }

  def post(path: URI, data: JsValue): Option[JsValue] = {
    val request = Http(path.toString)
      .method(POST.toString)
      .header("Accept", "application/json")
      .header("Content-type", "application/json")
      .charset("utf-8")
      .postData(data.toString)

    response(request)
  }

  def delete(path: URI): Option[JsValue] = {
    val request = Http(path.toString)
      .method(DELETE.toString)
      .header("Accept", "application/json")
      .charset("utf-8")

    response(request)
  }

  def getBasePath(appName: String, path: String): URI = {
    val url = s"${Config.wowzaEndpoint}:${Config.wowzaApiPort}/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/$appName/$path"
    URI.create(url)
  }
}

object WowzaIncomingStream {
  def list(appName: String): Future[List[IncomingStream]] = Future {
    val path = Request.getBasePath(appName, "instances/_definst_")
    val response = Request.get(path)

    (response.get \ "incomingStreams").as[List[IncomingStream]]
  }
}

object WowzaOutgoingStream {
  def list(appName: String): Future[List[OutgoingStream]] = Future {
    val path = Request.getBasePath(appName, "pushpublish/mapentries")
    val response = Request.get(path)

    (response.get \ "mapEntries").as[List[OutgoingStream]]
  }

  def create(appName: String, profile: OutgoingStream) = {
    val path = Request.getBasePath(appName, s"pushpublish/mapentries/${profile.entryName.replace(" ", "")}")
    Request.post(path, Json.toJson(profile))
  }

  def delete(appName: String, profile: OutgoingStream) = {
    val path = Request.getBasePath(appName, s"pushpublish/mapentries/${profile.entryName.replace(" ", "")}")
    Request.delete(path)
  }
}
