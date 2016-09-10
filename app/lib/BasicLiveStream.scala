package lib

import java.net.URI

import com.google.api.services.youtube.model.LiveStream

case class BasicLiveStream (streamName: String, host: String, applicationName: String)

object BasicLiveStream  {
  def build(stream: LiveStream) = {
    val ingestionInfo = stream.getCdn.getIngestionInfo
    val ingestionAddress = new URI(ingestionInfo.getIngestionAddress)
    BasicLiveStream (ingestionInfo.getStreamName, ingestionAddress.getHost, ingestionAddress.getPath)
  }
}
