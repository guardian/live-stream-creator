package lib

import java.io.FileNotFoundException
import java.net.URI

import com.amazonaws.auth.{AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{RegionUtils, Regions}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient

import scala.io.Source

object Config {
  implicit val stage : String = {
    try {
      val stageFile = Source.fromFile("/etc/gu/stage")
      val stage = stageFile.getLines.next
      stageFile.close()
      if (List("PROD", "CODE").contains(stage)) stage else "DEV"
    }
    catch {
      case e: FileNotFoundException => "DEV"
    }
  }

  val properties = Properties.fromPath("/etc/gu/live-stream-creator.properties")

  val youtubeClientId = properties("youtube.clientId")

  val youtubeClientSecret = properties("youtube.clientSecret")

  val youtubeRefreshToken = properties("youtube.refreshToken")

  val youtubeContentOwner: Option[String] = properties.get("youtube.contentOwner").filterNot(_.isEmpty)

  val isContentOwnerMode = youtubeContentOwner.isDefined

  val youtubeAppName = "gu-live-stream-creator"

  private val wowzaInternalEndpoint = properties("wowza.internalEndpoint")

  val domainRoot = stage match {
    case "DEV" => "http://localhost:9000" // TODO be better!
    case _ => s"https://${properties("domain.root")}"
  }

  val wowzaApiEndpoint = s"http://$wowzaInternalEndpoint:8087"

  val liveVideoStreamEndpoint = stage match {
    case "DEV" => s"http://$wowzaInternalEndpoint:1935"
    case _ => s"https://${properties("stream.domain.root")}"
  }

  val apiUri = URI.create(s"$domainRoot/api")

  val awsRegion = RegionUtils.getRegion(properties.getOrElse("aws.region", Regions.EU_WEST_1.getName))

  val awsCredentialsProviderChain = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider(properties.getOrElse("aws.profile", "multimedia")),
    new InstanceProfileCredentialsProvider()
  )

  val dynamoTableName = properties("aws.dynamo.table")
}
