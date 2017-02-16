package controllers

import com.google.api.services.youtube.model.Channel
import lib.argo.ArgoHelpers
import lib.argo.model.EntityResponse
import model._
import play.api.cache.Cache
import play.api.mvc.{Action, Controller}

object YouTubeChannelController extends Controller with ArgoHelpers {
  private def wrapChannel(channel: Channel): EntityResponse[YouTubeChannel] = {
    EntityResponse(
      data = YouTubeChannel.build(channel)
    )
  }

  def list() = Action {
    Cache.getAs[Seq[Channel]]("channels") match {
      case Some(channels) => {
        respondCollection[EntityResponse[YouTubeChannel]](
          data = channels.map(wrapChannel)
        )
      }
      case None => respondNotFound("no channels found")
    }
  }
}
