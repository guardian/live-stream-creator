package controllers

import java.util
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeTokenRequest, GoogleAuthorizationCodeFlow,
GoogleCredential, GoogleIdToken, GoogleIdTokenVerifier}

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTubeScopes
import play.api.mvc.RequestHeader
import utils.credentials
import play.api.Play
import play.api.Logger

/*
Common code relating to google auth
 */
trait GoogleAuthTrait {
  val JSON_FACTORY = JacksonFactory.getDefaultInstance
  val HTTP_TRANSPORT = new NetHttpTransport()
  val APP_CREDENTIALS_FILE = "/etc/credentials.json"
  val SESSION_ID_KEY = "livestream-sessid"
  val OAUTH2_CALLBACK_PATH = routes.GoogleAuthController.oauth2callback().toString()
  val NEXT_PAGE_KEY = "next-page"
  val ACCESS_TOKEN_KEY = "actok"

  def oauth2_redirect_url: String = {
    Play.current.configuration.getString("application.base_uri").get + OAUTH2_CALLBACK_PATH
  }

  /*
  This method checks to see if we have a session id ("user id") and attempts to load the credentials for it.
  if so, then it returns Right with the credentials, if not then Left with a Google generated redirect URL
   */
  def performAuth(request: RequestHeader): AnyRef = {
    val credential = new GoogleCredential

    request.session.get(SESSION_ID_KEY).map {
      sid: String=>
        val flow = newFlow()
        Logger.debug("performAuth: session id is " + sid)
        request.session.get(ACCESS_TOKEN_KEY) match {
          case None =>
            Logger.debug("No access token key in "+ ACCESS_TOKEN_KEY)
            Left (flow.newAuthorizationUrl().setRedirectUri(oauth2_redirect_url).build () )
          case id_token_string: Option[String] =>
            val credential = new GoogleCredential().setAccessToken(id_token_string.get)
            Logger.debug("performAuth: Got credential " + credential + " from session id " + sid)
            Right(credential)
          case _ =>
            Logger.debug("performAuth: could not load credential for session id " + sid)
            Left (flow.newAuthorizationUrl().setRedirectUri(oauth2_redirect_url).build () )
        }
    }.getOrElse {
      Logger.debug("performAuth: No session id present.")
      Left(newFlow().newAuthorizationUrl().setRedirectUri(oauth2_redirect_url).build())
    }
  }

  /*
  Helper method to return a new Flow object with the app's credentials
   */
  def newFlow() = {
    val credentialMap = credentials(APP_CREDENTIALS_FILE).read() //throws exception if credentials can't be loaded.

    new GoogleAuthorizationCodeFlow(HTTP_TRANSPORT,JSON_FACTORY,
      credentialMap("client_id"),
      credentialMap("client_secret"),
      util.Collections.singleton(YouTubeScopes.YOUTUBE)
    )
  }

}
