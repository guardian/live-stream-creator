package controllers

import java.net.URI

import lib._
import lib.argo.ArgoHelpers
import lib.argo.model.EntityResponse
import model.WowzaIncomingStream
import play.api.cache.Cache
import play.api.mvc.{Action, Controller}

import play.api.Play.current

object WowzaIncomingController extends Controller with ArgoHelpers {
  private def wrapStream(stream: WowzaIncomingStream): EntityResponse[WowzaIncomingStream] = {
    EntityResponse(data = stream)
  }

  def list(appName: String) = Action {
    Cache.getAs[Seq[WowzaIncomingStream]]("streams") match {
      case Some(streams) => {
        val uri = URI.create(s"${Config.domainRoot}/wowza/incoming/list/$appName")

        respondCollection[EntityResponse[WowzaIncomingStream]](
          uri = Some(uri),
          data = streams.map(wrapStream)
        )
      }
      case None => respondNotFound("no incoming streams found")
    }
  }
}
