package controllers

import com.google.api.services.youtube.model.Channel
import lib._
import lib.argo.ArgoHelpers
import lib.argo.model.EntityResponse
import model._
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global

object YouTubeChannelController extends Controller with ArgoHelpers {
  private def wrapChannel(channel: Channel): EntityResponse[YouTubeChannel] = {
    EntityResponse(
      data = YouTubeChannel.build(channel)
    )
  }

  def list() = Action.async {
    val channelFuture = YouTubeChannelApi.list()

    channelFuture.map[Result]((channels: Seq[Channel]) => {
      channels match {
        case Nil => respondNotFound("no channels found")
        case channel :: _ => {
          respondCollection[EntityResponse[YouTubeChannel]](
            data = channels.map(wrapChannel)
          )
        }
      }
    })
  }
}
