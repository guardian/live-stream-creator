package controllers

import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.{LiveStreamSnippet, LiveStreamContentDetails, CdnSettings, LiveStream}
import controllers.YTStreamsController._
import play.api.Logger

case class YTStream(frame_rate: String,
                    resolution: String,
                    ingestion_type: String,
                    reusable: Boolean,
                    title: String,
                    description: String,
                    channel_id: String) {

  /*
  Calls the youtube API to create a new stream with the given parameters
  @param: credential - Google oauth credentials to talk to youtube (requires the YouTube scope)

   */
  def make(credential: Credential) = {
    Logger.debug("YTStream::Make")
    Logger.debug("title: " + title)
    Logger.debug("description: " + description)
    Logger.debug("channel id: " + channel_id)

    val ls = new LiveStream()
    val cdn_settings = new CdnSettings()
    cdn_settings.setFrameRate(frame_rate)
    cdn_settings.setResolution(resolution)
    cdn_settings.setIngestionType(ingestion_type)
    ls.setCdn(cdn_settings)
    val details = new LiveStreamContentDetails()
    details.setIsReusable(reusable)
    ls.setContentDetails(details)
    val snip = new LiveStreamSnippet()
    snip.setTitle(title)
    snip.setDescription(description)
    snip.setChannelId(channel_id)
    ls.setSnippet(snip)
    ls.setKind("youtube#liveStream")

    val yt = new YouTube.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential.asInstanceOf[Credential]).setApplicationName("scala_youtube_test").build()
    yt.liveStreams().insert("snippet,cdn,contentDetails",ls).execute()
  }
}
