import scalariform.formatter.preferences._

name := "recommender-core"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10" % "test",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
  "org.apache.spark" % "spark-core_2.11" % "2.2.0",
  "org.apache.spark" % "spark-mllib_2.11" % "2.2.0",
  "ch.qos.logback" % "logback-classic" % "1.1.6"
)

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "hello"
  )

mainClass in Compile := Some("co.com.gamerecommender.startup.Startup")

enablePlugins(JavaServerAppPackaging)

packageName in Universal := name.value

parallelExecution in Test := false
fork in Test := false

        
