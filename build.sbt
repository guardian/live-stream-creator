import sbt.Keys._

name := "live-stream-creator"

version := "1.0"

/*i am not sure if we need this or not?*/
//lazy val root = (project in file(".")).enablePlugins(PlayScala)

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
        )
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


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

riffRaffPackageType := (packageZipTarball in Universal).value

def env(key: String): Option[String] = Option(System.getenv(key))

/* //commenting these out as they should be set above
riffRaffBuildIdentifier := env("CIRCLE_BUILD_NUM").getOrElse("DEV")

riffRaffUploadArtifactBucket := Option("riffraff-artifact")

riffRaffUploadManifestBucket := Option("riffraff-builds")

*/
