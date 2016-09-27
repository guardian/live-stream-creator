package lib.argo

import java.net.URI

import lib.argo.model._
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Result, Results}
import play.api.Logger

trait ArgoHelpers extends Results {

  val ArgoMediaType = "application/vnd.argo+json"

  def respond[T](data: T, links: List[Link] = Nil, actions: List[Action] = Nil, uri: Option[URI] = None)
                (implicit writes: Writes[T]): Result = {
    val response = EntityResponse(
      uri     = uri,
      data    = data,
      links   = links,
      actions = actions
    )

    serializeAndWrap(response, Ok)
  }

  def respondCollection[T](data: Seq[T], offset: Option[Long] = None, total: Option[Long] = None,
                           links: List[Link] = Nil, uri: Option[URI] = None)
                          (implicit writes: Writes[T]): Result = {
    val response = CollectionResponse(
      uri    = uri,
      offset = offset,
      length = Some(data.size),
      total  = total,
      data   = data,
      links  = links
    )

    serializeAndWrap(response, Ok)
  }

  // TODO: find a nicer way to serialise ErrorResponse[Nothing] without this hack
  def respondError(errorStatus: Status, errorKey: String, errorMessage: String, links: List[Link] = Nil): Result = {
    Logger.warn(s"Responding with error status ${errorStatus.header.status}, $errorMessage")
    val response = ErrorResponse[Int](
      errorKey     = errorKey,
      errorMessage = errorMessage,
      data         = None,
      links        = links
    )

    serializeAndWrap(response, errorStatus)
  }

  def respondNotFound(errorMessage: String): Result = {
    val response = ErrorResponse[Int](
      errorKey     = "not-found",
      errorMessage = errorMessage,
      data         = None,
      links        = Nil
    )

    serializeAndWrap(response, Status(404))
  }


  private def serializeAndWrap[T](response: T, status: Status)(implicit writes: Writes[T]): Result = {
    val json = Json.toJson(response)
    status(json).as(ArgoMediaType)
  }

}
