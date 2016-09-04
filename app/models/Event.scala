package models

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.github.nscala_time.time.Imports._
import com.gu.scanamo._

case class Event (start_time: DateTime,
                      channel_id: String,
                      creator: String,
                      contacts: Seq[String],
                      output_channels: Seq[String],
                      status: String
                        ) {
  /*
  Implicit converter to change joda datetime into a Long of epoch time for storage in dynamo
   */
  implicit val jodaNumberFormat = DynamoFormat.coercedXmap[DateTime, Long, IllegalArgumentException](new DateTime(_))(_.getMillis)

  /*
  returns either Left(error) or Right(object) depending on whether save succeeded or not
   */
  def save(configuration: play.api.Configuration) = {
    val client = new AmazonDynamoDBClient()
    client.setRegion(Region.getRegion(Regions.EU_WEST_1))

    Scanamo.put(client)(configuration.underlying.getString("table.events"))(this)
  }
}

