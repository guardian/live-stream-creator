name := "live-stream-creator"

version := "1.0"

scalaVersion := "2.11.6"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
    cache,
    ws,
    filters
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
