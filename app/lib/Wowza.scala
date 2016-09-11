package lib

import java.net.URI

import models.YouTubeLiveStream
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scalaj.http._

case class IncomingStream (
  name: String,
  source: String
)

object IncomingStream {
  implicit val incomingStreamReads: Reads[IncomingStream] = (
    (__ \ "name").read[String] ~
    (__ \ "sourceIp").read[String]
  )(IncomingStream.apply _)
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
) {
  def build (incomingStream: IncomingStream, youtubeLiveStream: YouTubeLiveStream) = {
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
}

case class HttpMethod(verb: String) {
  override def toString: String = verb
}
object GET extends HttpMethod("GET")
object POST extends HttpMethod("POST")
object DELETE extends HttpMethod("DELETE")

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
  def list(appName: String) = {
    val path = Request.getBasePath(appName, "instances/_definst_")
    val response = Request.get(path)

    (response.get \ "incomingStreams").as[List[IncomingStream]]
  }
}

object WowzaOutgoingStream {
  def list(appName: String) = {
    val path = Request.getBasePath(appName, "pushpublish/mapentries")
    val response = Request.get(path)

    (response.get \ "mapEntries").as[List[OutgoingStream]]
  }

  def create(appName: String, profile: OutgoingStream) = {
    val path = Request.getBasePath(appName, s"pushpublish/mapentries/${profile.entryName}")
    Request.post(path, Json.toJson(profile))
  }

  def delete(appName: String, profile: OutgoingStream) = {
    val path = Request.getBasePath(appName, s"pushpublish/mapentries/${profile.entryName}")
    Request.delete(path)
  }
}
