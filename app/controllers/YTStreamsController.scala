package controllers

import java.io.FileInputStream
import java.util

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTube.Builder
import play.api.mvc.{Action, Controller}

object YTStreamsController extends Controller with GoogleAuthTrait {
  object Resolutions{
    val RES_1080 = "1080p"
    val RES_720 = "720p"
  }

  object IngestProtocols {
    val IN_DASH = "dash"
    val IN_RTMP = "rtmp"
  }

  object FrameRates {
    val RATE_25 = "25fps"
    val RATE_50 = "50fps"
    val RATE_30 = "30fps"
    val RATE_60 = "60fps"
  }

  def do_auth(creds_json_stream: FileInputStream, scope: String) = {
    val credential = GoogleCredential.fromStream(creds_json_stream).createScoped(util.Collections.singleton(scope))

    new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("scala_youtube_test").build()
  }

  def list_channel(channel_id: String) = Action { request =>
    performAuth(request) match {
      case Right(credential)=>
        val yt = new YouTube.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential.asInstanceOf[Credential]).setApplicationName("scala_youtube_test").build()
        val request = yt.channels().list("contentDetails")
        request.setId(channel_id)
        val response = request.execute()
        Ok(response.getItems.toString)

      case Left(redirect_uri)=>
        Forbidden("Not logged in")
    }

  }
}
