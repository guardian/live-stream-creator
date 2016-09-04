package controllers

import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import javax.inject._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

class Events @Inject()(configuration: play.api.Configuration)
  extends Controller {

  val eventForm = Form(
    mapping(
      "start_time"->jodaDate,
      "channel_id"->nonEmptyText,
      "creator"->nonEmptyText,
      "contacts"->seq(text),
      "output_channels"->seq(text),
      "status"->text
    )(Event.apply)(Event.unapply)
  )

  def add_form = Action {
    Ok(views.html.eventform(eventForm))
  }

  /*
  saves a new record, with no pre-existing ID
   */
  def create_event = Action { implicit request =>
    eventForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.eventform(formWithErrors))
      },
      event_data => {
        event_data.save(configuration)
      }
    )
    Redirect(routes.Events.event_list())
  }

  /*
  updates an existing record
   */
  def update_event(eventid:String) = Action { implicit request =>
    eventForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.eventform(formWithErrors))
      },
      event_data => {
        event_data.save(configuration)
      }
    )
    Redirect(routes.Events.event_list())
  }


  /*
  output a list of known events
   */
  def event_list = Action { implicit request =>

    Ok(views.html.eventlist(LiveEventCollection.all(configuration,10)))
  }
}
