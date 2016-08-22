import sbt.Keys._

name := "LiveStreamCreator"

version := "1.0"

lazy val `livestreamcreator` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies += "com.google.api-client" % "google-api-client" % "1.22.0"

libraryDependencies += "com.google.apis" % "google-api-services-youtube" % "v3-rev178-1.22.0"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
