package controllers

import argonaut.Argonaut._
import argonaut.{Json, CodecJson}
import com.gu.scanamo.error.InvalidPropertiesError
import play.api.mvc._
import models._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject
import com.github.nscala_time.time.Imports.DateTime

/*
start_time: DateTime,
                      channel_id: String,
                      creator: String,
                      contacts: List[String],
                      output_channels: List[String],
                      status: String
 */

object Events
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
  //implicit lazy val JsonDateFormatter= argonaut.EncodeJson[com.github.nscala_time.time.Imports.DateTime](Codec)
//  implicit lazy val JsonDataCodec: CodecJson[com.github.nscala_time.time.Imports.DateTime] =
//    casecodec1(DateTime.parse(_),_.toString)
/*  implicit lazy val EventCodec: CodecJson[Event]=casecodec6(Event.apply, Event.unapply)(
    "start_time",
    "channel_id",
    "creator",
    "contacts",
    "output_channels",
    "status"
  )
*/
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


  /*
  output a list of known events
   */
  def event_list = Action { implicit request =>
    println(LiveEventCollection.all(10))
//    val events:List[Event] = LiveEventCollection.all(10,configuration).map(
//      (_ match {
//      case event_data: Event => _
//      case _ =>
//    }).map(
//      _.asInstanceOf[Event]
//      ))


    Ok(views.html.eventlist(LiveEventCollection.all(10)))
  }
}

