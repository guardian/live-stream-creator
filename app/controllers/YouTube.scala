package controllers

import java.net.URI

import com.google.api.services.youtube.model.{Channel, LiveBroadcast}
import lib.argo.ArgoHelpers
import lib._
import lib.argo.model.EntityResponse
import model.YouTubeChannel
import models._
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global

object YouTubeChannelController extends Controller with ArgoHelpers {
  private def wrapChannel(channel: Channel): EntityResponse[YouTubeChannel] = {
    EntityResponse(
      uri = Some(URI.create(s"${Config.domainRoot}/youtube/channel/${channel.getId}")),
      data = YouTubeChannel.build(channel)
    )
  }

  def get(channelId: String) = Action.async {
    val channelFuture = YouTubeChannelApi.get(channelId)

    channelFuture.map[Result] {
      case Some(channel) => {
        respond[EntityResponse[YouTubeChannel]](wrapChannel(channel))
      }
      case None => respondNotFound("no channel found")
    }
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
