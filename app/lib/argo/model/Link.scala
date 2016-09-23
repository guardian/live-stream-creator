package lib.argo.model

import java.net.URI

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.utils.UriEncoding

case class Link(rel: String, href: URI)

object Link {

  implicit val linkWrites: Writes[Link] = (
    (__ \ "rel").write[String] ~
      (__ \ "href").write[String].contramap((uri: URI) => UriEncoding.decodePath(uri.toString, "UTF-8"))
    )(unlift(Link.unapply))

}
