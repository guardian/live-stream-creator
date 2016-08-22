package controllers

import play.api.mvc._
import play.api.Logger

object GoogleAuthController extends Controller with GoogleAuthTrait {

  def testGoogleAuth = controllers.GoogleAuthAction {

    Ok("working")
  }

  def oauth2callback = Action { request=>
    val code = request.queryString("code").head //querystring paremeters are always a list of strings but we're only expecting one
    Logger.debug("oauth2callback: got code " + code)

    val flow=newFlow()
    val access_token = flow.newTokenRequest(code).setRedirectUri(oauth2_redirect_url).execute()
    val sid = request.session.get(SESSION_ID_KEY).get
    Logger.debug("oauth2callback: trying to store credential against " + sid)
    val credential = flow.createAndStoreCredential(access_token,sid)

    request.session.get(NEXT_PAGE_KEY).map {  //if we have a next-page then bump the user on to that
      case path: String => Redirect(path).withSession(request.session
        + (ACCESS_TOKEN_KEY -> credential.getAccessToken)
        - NEXT_PAGE_KEY)
      case _ =>Ok("Logged in")
    }.get
  }

  def logout = Action { request=>
    Ok("Logged out").withNewSession //blank out old session
  }
}
