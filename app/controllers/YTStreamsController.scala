package controllers

import java.io.FileInputStream
import java.util

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.{LiveStreamSnippet, LiveStreamContentDetails, CdnSettings, LiveStream}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller}
import play.api.Logger


object YTStreamsController extends Controller with GoogleAuthTrait {
  /*
  This performs authentication with a service account credential - it does not work with YT live!
   */
  def do_auth(creds_json_stream: FileInputStream, scope: String) = {
    val credential = GoogleCredential.fromStream(creds_json_stream).createScoped(util.Collections.singleton(scope))

    new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("scala_youtube_test").build()
  }

  /*
  Testing function - return JSON information about the channel given
   */
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

  /*
  Definition of a form for testing build of a channel
   */
  val streamForm = Form(
    mapping(
      "frame_rate"->nonEmptyText,
      "resolution"->nonEmptyText,
      "ingestion_type"->nonEmptyText,
      "reusable"->boolean,
      "title"->nonEmptyText,
      "description"->text,
      "channel_id"->nonEmptyText
    )(YTStream.apply)(YTStream.unapply)
  )

  /*
  Returns a rendered empty form
   */
  def create_stream_get = GoogleAuthAction { request =>
    Ok(views.html.stream_form(streamForm))
  }

  /*
  Processes form data
   */
  def create_stream_post = Action { request =>
    performAuth(request) match {
      case Right(credential)=>
        streamForm.bindFromRequest(request.body.asFormUrlEncoded.get).fold(
          formWithErrors => {
            Logger.debug("Errors in form submission " + formWithErrors)
            BadRequest(views.html.stream_form(formWithErrors))
          },
          YTStream => {
            Logger.debug("got valid stream make request")
            val result = YTStream.make(credential.asInstanceOf[Credential])
            Logger.debug("youtube returned " + result.toString)
            Ok(result.toString)
          }
        )
      case Left(redirect)=>
        Forbidden("You are not logged in")
    }
  }

/*
deprecated, doing same as above with json
 */
  def create_stream = Action { request =>
  performAuth(request) match {
    case Right(credential)=>
      val jsonBody: Option[JsValue] = request.body.asJson

      jsonBody.map { json=>
        val ls = new LiveStream()
        val cdn_settings = new CdnSettings()
        cdn_settings.setFrameRate((json \ "frame_rate").as[String])
        cdn_settings.setResolution((json \ "resolution").as[String])
        cdn_settings.setIngestionType((json \ "ingestion_type").as[String])
        ls.setCdn(cdn_settings)
        val details = new LiveStreamContentDetails()
        details.setIsReusable((json \ "reusable").as[Boolean])
        ls.setContentDetails(details)
        val snip = new LiveStreamSnippet()
        snip.setTitle((json \ "title").as[String])
        snip.setDescription((json \ "description").as[String])
        snip.setChannelId((json \ "channel_id").as[String])
        ls.setKind("youtube#liveStream")

        val yt = new YouTube.Builder(HTTP_TRANSPORT,JSON_FACTORY,credential.asInstanceOf[Credential]).setApplicationName("scala_youtube_test").build()
        val result = yt.liveStreams().insert("snippet,cdn,contentDetails",ls).execute()
        Ok(result.toString)
      }.getOrElse {
        BadRequest("Expecting a body of application/json")
      }
  }
  }
}


