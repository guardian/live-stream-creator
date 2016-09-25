package controllers

import lib.Config
import play.api.mvc._

object Application extends Controller {
  def index(ignored: String) = Action {
    Ok(views.html.main(Config.apiUri))
  }
}
