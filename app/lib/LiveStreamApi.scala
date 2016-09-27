package lib

import com.google.api.services.youtube.model.LiveStream
import model._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object LiveStreamApi {
  private def transform[T](fut: Future[Option[T]]): Future[T] = fut.filter(_.isDefined).map(_.get)

  def create(request: YouTubeLiveStreamCreateRequest): Future[YouTubeLiveStream] = {
    for {
      incomingStream <- transform(WowzaIncomingStreamApi.get(request.wowzaApplicationInstance, request.wowzaApp, request.wowzaStream))
      channel <- transform(YouTubeChannelApi.get(request.channel))
      broadcast <- YouTubeBroadcastApi.create(channel, request.title)
      boundStream: LiveStream <- YouTubeStreamApi.create(broadcast)
      outgoingStreamRequest = WowzaOutgoingStream.build(incomingStream, boundStream, enabled = true)
      liveStream = YouTubeLiveStream.build(channel, boundStream, broadcast, request.wowzaApp, outgoingStreamRequest)
      outgoingStream <- WowzaOutgoingStreamApi.create(request.wowzaApp, outgoingStreamRequest)

      saved = DataStore.create(liveStream)

    } yield liveStream
  }

  def monitor(id: String, request: YouTubeLiveStreamMonitorRequest): Future[YouTubeLiveStream] = {
    for {
      stream: YouTubeLiveStream <- transform(get(id))
      ytChannel <- transform(YouTubeChannelApi.get(stream.channel.id))
      ytStream <- transform(YouTubeStreamApi.get(ytChannel, stream.id))
      ytBroadcast <- transform(YouTubeBroadcastApi.get(ytChannel, stream.broadcastId))
      updatedBroadcast <- transform(YouTubeBroadcastApi.monitor(ytChannel, ytBroadcast))
    } yield stream
  }

  def start(id: String, request: YouTubeLiveStreamStartRequest): Future[YouTubeLiveStream] = {
    for {
      stream <- transform(get(id))
      ytChannel <- transform(YouTubeChannelApi.get(stream.channel.id))
      ytStream <- transform(YouTubeStreamApi.get(ytChannel, stream.id))
      ytBroadcast <- transform(YouTubeBroadcastApi.get(ytChannel, stream.broadcastId))
      updatedBroadcast <- transform(YouTubeBroadcastApi.start(ytChannel, ytBroadcast))
    } yield stream
  }

  def stop(id: String, request: YouTubeLiveStreamStopRequest): Future[YouTubeLiveStream] = {
    for {
      stream <- transform(get(id))
      ytChannel <- transform(YouTubeChannelApi.get(stream.channel.id))
      ytStream <- transform(YouTubeStreamApi.get(ytChannel, stream.id))
      ytBroadcast <- transform(YouTubeBroadcastApi.get(ytChannel, stream.broadcastId))
      updatedBroadcast <- transform(YouTubeBroadcastApi.stop(ytChannel, ytBroadcast))
      _ = WowzaOutgoingStreamApi.delete(stream.wowzaApp, stream.wowzaOutgoingStream)
    } yield stream
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
      ytChannel <- transform(YouTubeChannelApi.get(stream.channel.id))
      ytStream <- transform(YouTubeStreamApi.get(ytChannel, stream.id))
      ytBroadcast <- transform(YouTubeBroadcastApi.get(ytChannel, stream.broadcastId))
      streamStatus = YouTubeStreamApi.getStatus(ytStream)
      broadcastStatus = YouTubeBroadcastApi.getStatus(ytBroadcast)
    } yield YouTubeStreamHealthStatus(streamStatus, broadcastStatus.toString)
  }
}
