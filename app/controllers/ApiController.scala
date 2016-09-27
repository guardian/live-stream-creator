package controllers

import lib.ApiUtil
import lib.argo.ArgoHelpers
import lib.argo.model.Link
import play.api.libs.json.Writes
import play.api.mvc.{Action, Controller}
import play.api.libs.json._

case class ApiIndex(name: String, description: String)
object ApiIndex {
  implicit def jsonWrites: Writes[ApiIndex] = Json.writes[ApiIndex]
}

object ApiController extends Controller with ArgoHelpers {

  def index() = Action {
    val indexData = ApiIndex("live-stream-creator-api", "live stream creator api")

    val indexLinks = List(
      Link("streams", ApiUtil.getApiUri("streams")),
      Link("stream", ApiUtil.getApiUri("stream/{id}")),
      Link("stream-health", ApiUtil.getApiUri("stream/{id}/health")),
      Link("incoming-wowza", ApiUtil.getApiUri("incoming/{id}")),
      Link("outgoing-youtube", ApiUtil.getApiUri("outgoing/youtube"))
    )

    respond(indexData, indexLinks)
  }
}
