package lib

import java.net.URI

import model.{WowzaRawIncomingStream, WowzaIncomingStream, WowzaOutgoingStream}
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait WowzaApi {
  def getBasePath(appName: String, path: String): URI = {
    val url = s"${Config.wowzaInternalEndpoint}:${Config.wowzaApiPort}/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/$appName/$path"
    URI.create(url)
  }
}

object WowzaIncomingStreamApi extends WowzaApi {
  def get(applicationInstance: String, appName: String, streamName: String): Future[Option[WowzaIncomingStream]] = Future {
    val path = getBasePath(appName, s"instances/${applicationInstance}/incomingstreams/$streamName")


    Request.get(path).map(json => Some(json.as[WowzaRawIncomingStream])).map {
      case Some(s) => WowzaIncomingStream.build(appName, s)
    }
  }

  def list(appName: String): Future[List[WowzaIncomingStream]] = Future {
    val path = getBasePath(appName, "instances")
    val response = Request.get(path)

    val streams = (response.get \\ "incomingStreams").flatMap(y => y.as[List[WowzaRawIncomingStream]]).toList

    streams.map(s => WowzaIncomingStream.build(appName, s))
  }
}

object WowzaOutgoingStreamApi extends WowzaApi {
  def get(appName: String, streamName: String): Future[Option[WowzaOutgoingStream]] = Future {
    val path = getBasePath(appName, s"pushpublish/mapentries/$streamName")
    Request.get(path).map(json => Some(json.as[WowzaOutgoingStream])).getOrElse(None)
  }

  def list(appName: String): Future[List[WowzaOutgoingStream]] = Future {
    val path = getBasePath(appName, "pushpublish/mapentries")
    val response = Request.get(path)

    (response.get \ "mapEntries").as[List[WowzaOutgoingStream]]
  }

  def create(appName: String, profile: WowzaOutgoingStream) = Future {
    val path = getBasePath(appName, s"pushpublish/mapentries/${profile.safeEntryName}")
    Request.post(path, Json.toJson(profile))
  }

  def delete(appName: String, profile: WowzaOutgoingStream) = Future {
    val path = getBasePath(appName, s"pushpublish/mapentries/${profile.safeEntryName}")
    Request.delete(path)
  }

  def toggleState(appName: String, profile: WowzaOutgoingStream, enable: Boolean): Future[Option[JsValue]] = {
    val updatedProfile = profile.toggleEnabled(enable)
    update(appName, updatedProfile)
  }

  private def update(appName: String, profile: WowzaOutgoingStream): Future[Option[JsValue]] = Future {
    val path = getBasePath(appName, s"pushpublish/mapentries/${profile.safeEntryName}")
    Request.put(path, Json.toJson(profile))
  }
}
