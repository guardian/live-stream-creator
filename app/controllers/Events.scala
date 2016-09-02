package controllers

import play.api.mvc._
import models.Event
import play.api.data._
import play.api.data.Forms._

/*
start_time: DateTime,
                      channel_id: String,
                      creator: String,
                      contacts: List[String],
                      output_channels: List[String],
                      status: String
 */

object Events extends Controller {
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
        event_data.save()
      }
    )
    Ok("Save worked")
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
        event_data.save()
      }
    )
    Ok("Save worked")
  }

}
