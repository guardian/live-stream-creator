package lib

import com.google.api.services.youtube.model.{Channel, LiveBroadcast}
import model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object LiveStreamCreator {
  def transform[T](fut: Future[Option[T]]): Future[T] = fut.filter(_.isDefined).map(_.get)

  def create(request: YouTubeLiveStreamRequest) = {
    for {
      incomingStream <- transform(WowzaIncomingStreamApi.get(request.wowzaApp, request.wowzaStream))
      channel <- transform(YouTubeChannelApi.get(request.channel))
      broadcast <- YouTubeBroadcastApi.create(channel, request.title)
      boundBroadcast <- YouTubeStreamApi.create(broadcast)
      boundStream <- transform(YouTubeStreamApi.get(channel, boundBroadcast.getContentDetails.getBoundStreamId))
      liveStream = YouTubeLiveStream.build(boundStream)
      outgoingStream = WowzaOutgoingStream.build(incomingStream, liveStream, enabled = true)
      _ <- WowzaOutgoingStreamApi.create(request.wowzaApp, outgoingStream)
      completeBroadcast <- transform(YouTubeBroadcastApi.get(channel, broadcast.getId))

    } yield boundStream
  }
}
