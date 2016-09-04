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
        riffRaffArtifactResources := Seq(
            riffRaffPackageType.value -> s"packages/${name.value}/${riffRaffPackageType.value.getName}",
            baseDirectory.value / "conf/deploy.json" -> "deploy.json"
        ),
        riffRaffPackageType := (packageZipTarball in Universal).value,
        routesGenerator := InjectedRoutesGenerator
    )

scalaVersion := "2.11.6"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies ++= Seq(
    "com.gu" %% "scanamo" % "0.7.0",
    "com.google.api-client" % "google-api-client" % "1.22.0",
    "com.google.apis" % "google-api-services-youtube" % "v3-rev178-1.22.0",
    "com.github.nscala-time" %% "nscala-time" % "2.12.0",
    "com.google.inject" % "guice" % "3.0",
    "io.argonaut" %% "argonaut" % "6.1",
    "javax.inject" % "javax.inject" % "1"
)

libraryDependencies += specs2 % Test

libraryDependencies ++= Seq(
    cache,
    ws,
    filters,
    jdbc,
    cache,
    ws
)


def env(key: String): Option[String] = Option(System.getenv(key))
