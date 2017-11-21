import scalariform.formatter.preferences._

name := "recommender-core"

version := "0.1"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10" % "test",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
  "org.apache.spark" % "spark-core_2.11" % "2.2.0",
  "org.apache.spark" % "spark-mllib_2.11" % "2.2.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5",
  "neo4j-contrib" % "neo4j-spark-connector" % "2.1.0-M4",
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

        
