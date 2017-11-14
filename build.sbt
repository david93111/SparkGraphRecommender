name := "recommender-core"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.0" % "test"
)

enablePlugins(JavaServerAppPackaging)

packageName in Universal := name.value

parallelExecution in Test := false
fork in Test := false

        