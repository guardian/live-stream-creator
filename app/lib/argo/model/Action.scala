package lib.argo.model

import java.net.URI

import lib.HttpMethod
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.utils.UriEncoding

case class Action(name: String, href: URI, method: HttpMethod)

object Action {
  implicit val actionWrites: Writes[Action] = (
    (__ \ "name").write[String] ~
      (__ \ "href").write[String].contramap((uri: URI) => UriEncoding.decodePath(uri.toString, "UTF-8")) ~
      (__ \ "method").write[String].contramap((_: HttpMethod).toString)
    )(unlift(Action.unapply))
}
