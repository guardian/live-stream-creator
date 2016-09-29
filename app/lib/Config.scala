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

  val wowzaPublicEndpoint = properties("wowza.publicEndpoint")

  val wowzaInternalEndpoint = properties.getOrElse("wowza.internalEndpoint", wowzaPublicEndpoint)

  val wowzaApiPort = properties.getOrElse("wowza.port", "8087").toInt

  val wowzaStreamingPort = properties.getOrElse("wowza.streamingPort", "1935").toInt

  val domainRoot = stage match {
    case "DEV" => "http://localhost:9000" // TODO be better!
    case _ => s"https://${properties("domain.root")}"
  }

  val apiUri = URI.create(s"$domainRoot/api")

  val awsRegion = RegionUtils.getRegion(properties.getOrElse("aws.region", Regions.EU_WEST_1.getName))

  val awsCredentialsProviderChain = new AWSCredentialsProviderChain(
    new ProfileCredentialsProvider(properties.getOrElse("aws.profile", "multimedia")),
    new InstanceProfileCredentialsProvider()
  )

  val dynamoTableName = properties("aws.dynamo.table")
}
