package lib

import java.net.URI

import model.{WowzaIncomingStream, WowzaOutgoingStream}
import models._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait WowzaApi {
  def getBasePath(appName: String, path: String): URI = {
    val url = s"${Config.wowzaEndpoint}:${Config.wowzaApiPort}/v2/servers/_defaultServer_/vhosts/_defaultVHost_/applications/$appName/$path"
    URI.create(url)
  }
}

object WowzaIncomingStreamApi extends WowzaApi {
  def get(appName: String, streamName: String): Future[Option[WowzaIncomingStream]] = Future {
    val path = getBasePath(appName, s"instances/_definst_/incomingstreams/$streamName")
    Request.get(path).map(json => Some(json.as[WowzaIncomingStream])).getOrElse(None)
  }

  def list(appName: String): Future[List[WowzaIncomingStream]] = Future {
    val path = getBasePath(appName, "instances/_definst_")
    val response = Request.get(path)

    (response.get \ "incomingStreams").as[List[WowzaIncomingStream]]
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
