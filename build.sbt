import com.gu.riffraff.artifact.RiffRaffArtifact.autoImport._
import sbt.Keys._

name := "live-stream-creator"

version := "1.0"

lazy val `livestreamcreator` = (project in file("."))
    .enablePlugins(PlayScala, RiffRaffArtifact, UniversalPlugin)
    .settings(
        packageName in Universal := normalizedName.value,
        name in Universal := normalizedName.value,
        topLevelDirectory in Universal := Some(normalizedName.value),
        riffRaffPackageName := s"${name.value}",
        riffRaffManifestProjectName := riffRaffPackageName.value,
        riffRaffPackageType := (packageZipTarball in Universal).value,
        riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
        riffRaffUploadManifestBucket := Option("riffraff-builds"),
        riffRaffBuildIdentifier := env("CIRCLE_BUILD_NUM").getOrElse("DEV"),
        riffRaffManifestBranch := env("CIRCLE_BRANCH").getOrElse("DEV"),
        riffRaffManifestVcsUrl := "git@github.com:guardian/live-stream-creator.git"
    )

scalaVersion := "2.11.6"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0"

libraryDependencies += "com.google.apis" % "google-api-services-youtube" % "v3-rev178-1.22.0"

libraryDependencies ++= Seq(
    cache,
    ws,
    filters,
    jdbc,
    anorm,
    cache,
    ws
)

def env(key: String): Option[String] = Option(System.getenv(key))
