package controllers
import play.api.mvc._

object Application extends Controller {

  def healthcheck = Action {
    Ok("healthy")
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def test = Action {

    Ok("done")
  }
}