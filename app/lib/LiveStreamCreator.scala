package lib

import model._

import scala.concurrent.Future

object LiveStreamCreator {
  def transform[T](fut: Future[Option[T]]): Future[T] = fut.filter(_.isDefined).map(_.get)

  def create(request: YouTubeLiveStreamRequest): Future[None.type] = {
    val result: Future[Nothing] = for (
      incomingStream <- transform(WowzaIncomingStreamApi.get(request.wowzaStream));
      channel <- transform(YouTubeChannelApi.get(request.channel));
      broadcast <- YouTubeBroadcastApi.create(channel, request.title);
      boundBroadcast <- YouTubeStreamApi.create(broadcast);
      boundStream <- transform(YouTubeStreamApi.get(channel, boundBroadcast.getContentDetails.getBoundStreamId));
      liveStream <- YouTubeLiveStream.build(boundStream);
      outgoingStream <- WowzaOutgoingStream.build(incomingStream, liveStream, enabled = false);
      _ <- WowzaOutgoingStreamApi.create(Config.wowzaApplication, outgoingStream);
      completeBroadcast <- transform(YouTubeBroadcastApi.get(channel, broadcast.getId))

    ) yield(completeBroadcast.getContentDetails.getMonitorStream.getEmbedHtml)

    result recover {
      case _ => None
    }
  }
}
