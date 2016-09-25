package controllers

import java.net.URI

import lib._
import lib.argo.ArgoHelpers
import lib.argo.model.{EntityResponse, Link, Action => ArgoAction}
import model._
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LiveStreamController extends Controller with ArgoHelpers {
  private def getStreamUri(stream: YouTubeLiveStream, path: Option[String] = None): URI = {
    getStreamUri(stream.id, path)
  }

  private def getStreamUri(id: String, path: Option[String]): URI = {
    path match {
      case Some(p) => ApiUtil.getApiUri(s"stream/$id/$p")
      case _ => ApiUtil.getApiUri(s"stream/$id")
    }
  }

  private def wrapStream(stream: YouTubeLiveStream): EntityResponse[YouTubeLiveStream] = {
    EntityResponse(
      uri = Some(getStreamUri(stream)),
      data = stream,
      links = List(
        Link("healthcheck", getStreamUri(stream, Some("healthcheck")))
      ),
      actions = List(
        ArgoAction("monitor", getStreamUri(stream, Some("monitor")), PUT),
        ArgoAction("start", getStreamUri(stream, Some("start")), PUT),
        ArgoAction("stop", getStreamUri(stream, Some("stop")), PUT)
      )
    )
  }

  private def respondStream(stream: YouTubeLiveStream) = {
    val uri = Some(getStreamUri(stream))
    val links = List(
      Link("healthcheck", getStreamUri(stream, Some("healthcheck")))
    )
    val actions = List(
      ArgoAction("monitor", getStreamUri(stream, Some("monitor")), PUT),
      ArgoAction("start", getStreamUri(stream, Some("start")), PUT),
      ArgoAction("stop", getStreamUri(stream, Some("stop")), PUT)
    )

    respond[YouTubeLiveStream](data=stream, links=links, actions=actions, uri=uri)
  }

  def get(streamId: String) = Action.async {
    LiveStreamApi.get(streamId).map[Result] {
      case Some(stream) => respondStream(stream)
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
          respondStream(stream)
        }}
      }
      case None => Future(respondError(BadRequest, "meep", "cannot deseralize request"))
    }
  }

  def healthcheck(streamId: String) = Action.async {
    LiveStreamApi.getStatus(streamId).map[Result] { status =>
      val uri = Some(getStreamUri(streamId, Some("health")))

      respond[YouTubeStreamHealthStatus](data=status, uri=uri)
    }
  }

  def monitor(streamId: String) = Action.async(parse.json) { request =>
    (request.body \ "data").asOpt[YouTubeLiveStreamMonitorRequest] match {
      case Some(updateRequest) => {
        LiveStreamApi.monitor(streamId, updateRequest).map { stream => {
          respondStream(stream)
        }}
      }
      case None => Future(respondError(BadRequest, "meep", "cannot deseralize request"))
    }
  }

  def start(streamId: String) = Action.async(parse.json) { request =>
    (request.body \ "data").asOpt[YouTubeLiveStreamStartRequest] match {
      case Some(startRequest) => {
        LiveStreamApi.start(streamId, startRequest).map { stream => {
          respond[EntityResponse[YouTubeLiveStream]](wrapStream(stream))
        }}
      }
      case None => Future(respondError(BadRequest, "meep", "cannot deseralize request"))
    }
  }

  def stop(streamId: String) = Action.async(parse.json) { request =>
    (request.body \ "data").asOpt[YouTubeLiveStreamStopRequest] match {
      case Some(stopRequest) => {
        LiveStreamApi.stop(streamId, stopRequest).map { stream => {
          respond[EntityResponse[YouTubeLiveStream]](wrapStream(stream))
        }}
      }
      case None => Future(respondError(BadRequest, "meep", "cannot deseralize request"))
    }
  }
}
