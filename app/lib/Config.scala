package lib

import java.io.FileNotFoundException
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

  val wowzaEndpoint = properties("wowza.endpoint")

  val wowzaApiPort = properties.getOrElse("wowza.port", "8087").toInt

  val wowzaApplication = "live" // TODO dynamically look this up via the wowza API
}
