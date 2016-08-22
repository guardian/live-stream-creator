package controllers

import play.api.mvc
import play.api.mvc._
import scala.concurrent.Future
import java.util

object GoogleAuthAction extends ActionBuilder[Request] with Results with GoogleAuthTrait {

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[mvc.Result]): Future[mvc.Result] = {
    performAuth(request) match {
      case Right(credential) =>
        block (request)
      case Left(new_url) =>
        if(request.session.get(SESSION_ID_KEY).isEmpty){
          Future.successful(
            Redirect(new_url.asInstanceOf[String]).withSession(
              request.session +
                (SESSION_ID_KEY -> util.UUID.randomUUID().toString) +
                (NEXT_PAGE_KEY -> request.path)
            )
          )
        } else {
          Future.successful(Redirect(new_url.asInstanceOf[String]).withSession(
            request.session +
              (NEXT_PAGE_KEY -> request.path)
            )
          )
        }
    }
  }
}
