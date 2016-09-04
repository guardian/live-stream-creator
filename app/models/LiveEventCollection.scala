package models

import cats.data.Xor
import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.gu.scanamo.{Scanamo, DynamoFormat}
import org.joda.time.DateTime

object LiveEventCollection  {
  implicit val jodaStringFormat = DynamoFormat.coercedXmap[DateTime, Long, IllegalArgumentException](new DateTime(_))(_.getMillis)

  def all(limit: Int) = {
    val client = new AmazonDynamoDBClient()
    client.setRegion(Region.getRegion(Regions.EU_WEST_1))

//    Scanamo.scanWithLimit[Event](client)(getProperty("com.gu.livestreamcreator.events_dynamo_table"), limit).map(
//      _ match {
//        case Xor.Right(event) => event
//        case Xor.Left(other) => throw new Exception(s"invalid data was received: $other")
//      }
//    )
    List[Event]()
  }

}
