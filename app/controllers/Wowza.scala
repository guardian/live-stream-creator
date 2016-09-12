package controllers

import java.net.URI

import lib._
import lib.argo.ArgoHelpers
import lib.argo.model.{EntityResponse, Action => ArgoAction}
import model.{WowzaIncomingStream, WowzaOutgoingStream}
import models._
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global

object WowzaIncomingController extends Controller with ArgoHelpers {
  private def wrapStream(stream: WowzaIncomingStream): EntityResponse[WowzaIncomingStream] = {
    EntityResponse(data = stream)
  }

  def list(appName: String) = Action.async {
    val streamFuture = WowzaIncomingStreamApi.list(appName)

    streamFuture.map[Result]((streams: Seq[WowzaIncomingStream]) => {
      streams match {
        case Nil => respondNotFound("no incoming streams found")
        case stream :: _ => {
          val uri = URI.create(s"${Config.domainRoot}/wowza/incoming/list/$appName")

          respondCollection[EntityResponse[WowzaIncomingStream]](
            uri = Some(uri),
            data = streams.map(wrapStream)
          )
        }
      }
    })
  }
}

object WowzaOutgoingController extends Controller with ArgoHelpers {
  private def wrapStream(stream: WowzaOutgoingStream): EntityResponse[WowzaOutgoingStream] = {
    val actions = List(
      ArgoAction("enable", URI.create(s"${Config.domainRoot}/wowza/outgoing/${stream.entryName}"), PUT),
      ArgoAction("delete", URI.create(s"${Config.domainRoot}/wowza/outgoing/${stream.entryName}"), DELETE)
    )

    EntityResponse(data = stream, actions = actions)
  }

  def list(appName: String) = Action.async {
    val streamFuture = WowzaOutgoingStreamApi.list(appName)

    streamFuture.map[Result]((streams: Seq[WowzaOutgoingStream]) => {
      streams match {
        case Nil => respondNotFound("no outgoing streams found")
        case stream :: _ => {
          val uri = URI.create(s"${Config.domainRoot}/wowza/outgoing/list/$appName")

          respondCollection[EntityResponse[WowzaOutgoingStream]](
            uri = Some(uri),
            data = streams.map(wrapStream)
          )
        }
      }
    })
  }
}
