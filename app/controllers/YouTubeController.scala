package controllers

import java.net.URI

import com.google.api.services.youtube.model.Channel
import lib.argo.ArgoHelpers
import lib.{Config, YouTubeChannel}
import lib.argo.model.EntityResponse
import models.YTChannel
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global

object YouTubeChannelController extends Controller with ArgoHelpers {
  private def wrapChannel(channel: Channel): EntityResponse[YTChannel] = {
    EntityResponse(
      uri = Some(URI.create(s"${Config.domainRoot}/youtube/channel/${channel.getId}")),
      data = YTChannel.build(channel)
    )
  }

  def get(channelId: String) = Action.async {
    val channelFuture = YouTubeChannel.get(channelId)

    channelFuture.map[Result] {
      case Some(channel) => {
        respond[EntityResponse[YTChannel]](wrapChannel(channel))
      }
      case None => respondNotFound("no channel found")
    }
  }

  def list() = Action.async {
    val channelFuture = YouTubeChannel.list()

    channelFuture.map[Result]((channels: Seq[Channel]) => {
      channels match {
        case Nil => respondNotFound("no channels found")
        case channel :: _ => {
          respondCollection[EntityResponse[YTChannel]](
            data = channels.map(wrapChannel)
          )
        }
      }
    })
  }
}
