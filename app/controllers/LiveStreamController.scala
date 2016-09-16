package controllers

import java.net.URI

import lib.{ApiUtil, Config, LiveStreamApi, PUT}
import lib.argo.ArgoHelpers
import lib.argo.model.{EntityResponse, Link, Action => ArgoAction}
import model._
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LiveStreamController extends Controller with ArgoHelpers {
  private def getStreamUri(stream: YouTubeLiveStream, path: Option[String] = None): URI = getStreamUri(stream.id, path)

  private def getStreamUri(id: String, path: Option[String]): URI = {
    path match {
      case Some(p) => URI.create(ApiUtil.getApiUrl(s"stream/$id/$p"))
      case _ => URI.create(ApiUtil.getApiUrl(s"stream/$id"))
    }
  }

  private def wrapStream(stream: YouTubeLiveStream): EntityResponse[YouTubeLiveStream] = {
    EntityResponse(
      uri = Some(getStreamUri(stream)),
      data = stream,
      links = List(
        Link("health", getStreamUri(stream, Some("health")).toString)
      ),
      actions = List(
        ArgoAction("monitor", getStreamUri(stream, Some("monitor")), PUT)
      )
    )
  }

  private def wrapStreamHealthStatus(streamId: String, healthStatus: YouTubeStreamHealthStatus): EntityResponse[YouTubeStreamHealthStatus] = {
    EntityResponse(
      uri = Some(getStreamUri(streamId, Some("health"))),
      data = healthStatus
    )
  }

  def get(streamId: String) = Action.async {
    LiveStreamApi.get(streamId).map[Result] {
      case Some(stream) => respond[EntityResponse[YouTubeLiveStream]](wrapStream(stream))
      case None => respondError(BadRequest, "meep", "no stream found")
    }
  }

  def list() = Action.async {
    LiveStreamApi.list().map[Result]((streams: Seq[YouTubeLiveStream]) => {
      streams match {
        case Nil => respondNotFound("no streams found")
        case stream :: _ => {
          respondCollection[EntityResponse[YouTubeLiveStream]](
            data = streams.map(wrapStream)
          )
        }
      }
    })
  }

  def create() = Action.async(parse.json){ request =>
    (request.body \ "data").asOpt[YouTubeLiveStreamCreateRequest] match {
      case Some(streamRequest) => {
        LiveStreamApi.create(streamRequest).map { stream => {
          respond[EntityResponse[YouTubeLiveStream]](wrapStream(stream))
        }}
      }
      case None => Future(respondError(BadRequest, "meep", "cannot deseralize request"))
    }
  }

  def getHealthStatus(streamId: String) = Action.async {
    LiveStreamApi.getStatus(streamId).map[Result] { status =>
      respond[EntityResponse[YouTubeStreamHealthStatus]](wrapStreamHealthStatus(streamId, status))
    }
  }

  def updateMonitor(streamId: String) = Action.async(parse.json) { request =>
    (request.body \ "data").asOpt[YouTubeLiveStreamUpdateRequest] match {
      case Some(updateRequest) => {
        LiveStreamApi.monitor(streamId, updateRequest).map { stream => {
          respond[EntityResponse[YouTubeLiveStream]](wrapStream(stream))
        }}
      }
      case None => Future(respondError(BadRequest, "meep", "cannot deseralize request"))
    }
  }
}
