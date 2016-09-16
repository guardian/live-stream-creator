package lib

import com.google.api.services.youtube.model.LiveStream
import model._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LiveStreamApi {
  private def transform[T](fut: Future[Option[T]]): Future[T] = fut.filter(_.isDefined).map(_.get)

  def create(request: YouTubeLiveStreamCreateRequest): Future[YouTubeLiveStream] = {
    for {
      incomingStream <- transform(WowzaIncomingStreamApi.get(request.wowzaApp, request.wowzaStream))
      channel <- transform(YouTubeChannelApi.get(request.channel))
      broadcast <- YouTubeBroadcastApi.create(channel, request.title)
      boundStream: LiveStream <- YouTubeStreamApi.create(broadcast)
      liveStream = YouTubeLiveStream.build(channel, boundStream, broadcast)
      outgoingStreamRequest = WowzaOutgoingStream.build(incomingStream, boundStream, enabled = true)
      outgoingStream <- WowzaOutgoingStreamApi.create(request.wowzaApp, outgoingStreamRequest)

      saved = DataStore.create(liveStream)

    } yield liveStream
  }

  def monitor(id: String, request: YouTubeLiveStreamUpdateRequest): Future[YouTubeLiveStream] = {
    for {
      stream <- transform(get(id))
      ytChannel <- transform(YouTubeChannelApi.get(stream.channelId))
      ytStream <- transform(YouTubeStreamApi.get(ytChannel, stream.id))
      ytBroadcast <- transform(YouTubeBroadcastApi.get(ytChannel, stream.broadcastId))
      updatedBroadcast <- transform(YouTubeBroadcastApi.startMonitor(ytChannel, ytBroadcast))
      updatedStream = YouTubeLiveStream.build(ytChannel, ytStream, updatedBroadcast)

//      saved = DataStore.update(updatedStream)

    } yield updatedStream
  }

  def get(id: String) = Future[Option[YouTubeLiveStream]] {
    DataStore.get(id)
  }

  def list() = Future[List[YouTubeLiveStream]] {
    DataStore.list()
  }

  def getStatus(id: String) = {
    for {
      stream <- transform(get(id))
      ytChannel <- transform(YouTubeChannelApi.get(stream.channelId))
      ytStream <- transform(YouTubeStreamApi.get(ytChannel, stream.id))
    } yield YouTubeStreamHealthStatus.build(ytStream.getStatus.getStreamStatus)
  }
}
