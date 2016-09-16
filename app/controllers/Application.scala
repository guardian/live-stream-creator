package controllers

import lib.Config
import play.api.mvc._

object Application extends Controller {
  def index = Action {
    Ok(views.html.main(Config.apiUri))
  }
}
