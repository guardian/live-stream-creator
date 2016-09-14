package controllers

import java.net.URI

import com.google.api.services.youtube.model.{Channel, LiveBroadcast, LiveStream}
import lib.argo.ArgoHelpers
import lib._
import lib.argo.model.EntityResponse
import model.{YouTubeChannel, YouTubeLiveStream, YouTubeLiveStreamRequest}
import models._
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

object YouTubeLiveStreamController extends Controller with ArgoHelpers {
  def create() = Action.async(parse.json){ request =>
    (request.body \ "data").asOpt[YouTubeLiveStreamRequest] match {
      case Some(streamRequest) => {
        LiveStreamCreator.create(streamRequest).map { (foo: LiveStream) => {
          val bar: YouTubeLiveStream = YouTubeLiveStream.build(foo)
          respond[EntityResponse[YouTubeLiveStream]](EntityResponse(data = bar))
        }}
      }
      case None => Future(respondError(BadRequest, "meep", "cannot deseralize request"))
    }
  }
}
