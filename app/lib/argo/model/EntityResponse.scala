package lib.argo.model

import java.net.URI

import lib.argo.WriteHelpers
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.utils.UriEncoding


case class EntityResponse[T](
  uri: Option[URI] = None,
  data: T,
  links: List[Link] = Nil,
  actions: List[Action] = Nil
)

object EntityResponse extends WriteHelpers {

  implicit def entityResponseWrites[T: Writes]: Writes[EntityResponse[T]] = (
    (__ \ "uri").writeNullable[String].contramap((maybeUri: Option[URI]) => {
      maybeUri.map(uri => UriEncoding.decodePath(uri.toString, "UTF-8"))
    }) ~
      (__ \ "data").write[T] ~
      (__ \ "links").writeNullable[List[Link]].contramap(someListOrNone[Link]) ~
      (__ \ "actions").writeNullable[List[Action]].contramap(someListOrNone[Action])
    )(unlift(EntityResponse.unapply[T]))

}
