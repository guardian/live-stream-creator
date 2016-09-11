package lib

import com.google.api.services.youtube.model.Channel
import models.YouTubeLiveStream

object Workflow {
  def create(incomingStream: IncomingStream, channel: Channel, title: String) = {
    val broadcast = YouTubeBroadcast.create(channel, title)
    val boundBroadcast = YouTubeStream.create(broadcast)
    val boundStream = YouTubeStream.get(channel, boundBroadcast.getContentDetails.getBoundStreamId).get
    val liveStream = YouTubeLiveStream.build(boundStream)

    val outgoingStream = OutgoingStream.build(incomingStream, liveStream)

    WowzaOutgoingStream.create("live", outgoingStream)

    YouTubeBroadcast.startMonitor(channel, boundBroadcast)

    YouTubeBroadcast.get(channel, broadcast.getId).map(_.getContentDetails.getMonitorStream.getEmbedHtml)
  }
}
