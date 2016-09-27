package lib.argo.model

import java.net.URI

import lib.argo.WriteHelpers
import play.api.libs.functional.syntax._
import play.api.libs.json._


case class EmbeddedEntity[T](
  uri: URI,
  data: Option[T],
  links: List[Link] = Nil,
  actions: List[Action] = Nil
)

object EmbeddedEntity extends WriteHelpers {

  implicit def embeddedEntityWrites[T: Writes]: Writes[EmbeddedEntity[T]] = (
    (__ \ "uri").write[String].contramap((_: URI).toString) ~
      (__ \ "data").writeNullable[T] ~
      (__ \ "links").writeNullable[List[Link]].contramap(someListOrNone[Link]) ~
      (__ \ "actions").writeNullable[List[Action]].contramap(someListOrNone[Action])
    )(unlift(EmbeddedEntity.unapply[T]))

}
