package models

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.github.nscala_time.time.Imports._
import com.gu.scanamo._
import com.gu.scanamo.syntax._
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType._

case class Event (start_time: DateTime,
                      channel_id: String,
                      creator: String,
                      contacts: Seq[String],
                      output_channels: Seq[String],
                      status: String
                        ){

  /*
  returns either Left(error) or Right(object) depending on whether save succeeded or not
   */
  def save() = {
    val client = new AmazonDynamoDBClient()
    client.setRegion(Region.getRegion(Regions.EU_WEST_1))

    implicit val jodaStringFormat = DynamoFormat.coercedXmap[DateTime, Long, IllegalArgumentException](new DateTime(_))(_.getMillis)

    Scanamo.put(client)("live-stream-creator-EventModelTable-VVXLQ8GMND5V")(this)
  }
}
