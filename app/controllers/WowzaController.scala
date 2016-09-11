package controllers

import java.net.URI

import lib._
import lib.argo.ArgoHelpers
import lib.argo.model.{EntityResponse, Action => ArgoAction}
import models._
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global

object WowzaIncomingController extends Controller with ArgoHelpers {
  private def wrapStream(stream: IncomingStream): EntityResponse[IncomingStream] = {
    EntityResponse(data = stream)
  }

  def list(appName: String) = Action.async {
    val streamFuture = WowzaIncomingStream.list(appName)

    streamFuture.map[Result]((streams: Seq[IncomingStream]) => {
      streams match {
        case Nil => respondNotFound("no incoming streams found")
        case stream :: _ => {
          val uri = URI.create(s"${Config.domainRoot}/wowza/incoming/list/$appName")

          respondCollection[EntityResponse[IncomingStream]](
            uri = Some(uri),
            data = streams.map(wrapStream)
          )
        }
      }
    })
  }
}

object WowzaOutgoingController extends Controller with ArgoHelpers {
  private def wrapStream(stream: OutgoingStream): EntityResponse[OutgoingStream] = {
    val actions = List(
      ArgoAction(
        "delete",
        URI.create(s"${Config.domainRoot}/wowza/outgoing/${stream.entryName}"),
        DELETE
      )
    )

    EntityResponse(data = stream, actions = actions)
  }

  def list(appName: String) = Action.async {
    val streamFuture = WowzaOutgoingStream.list(appName)

    streamFuture.map[Result]((streams: Seq[OutgoingStream]) => {
      streams match {
        case Nil => respondNotFound("no outgoing streams found")
        case stream :: _ => {
          val uri = URI.create(s"${Config.domainRoot}/wowza/outgoing/list/$appName")

          respondCollection[EntityResponse[OutgoingStream]](
            uri = Some(uri),
            data = streams.map(wrapStream)
          )
        }
      }
    })
  }
}
