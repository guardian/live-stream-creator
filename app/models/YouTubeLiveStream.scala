package models

import java.net.URI

import com.google.api.services.youtube.model.LiveStream

case class YouTubeLiveStream (
  streamName: String,
  host: String,
  applicationName: String
)

object YouTubeLiveStream {
  def build(stream: LiveStream) = {
    val ingestionInfo = stream.getCdn.getIngestionInfo
    val ingestionAddress = new URI(ingestionInfo.getIngestionAddress)
    YouTubeLiveStream(ingestionInfo.getStreamName, ingestionAddress.getHost, ingestionAddress.getPath)
  }
}
