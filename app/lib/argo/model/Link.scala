package lib.argo.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Link(rel: String, href: String)

object Link {

  implicit val linkWrites: Writes[Link] = (
    (__ \ "rel").write[String] ~
      (__ \ "href").write[String]
    )(unlift(Link.unapply))

}
