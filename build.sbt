import scalariform.formatter.preferences._

name := "recommender-core"

version := "0.2.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Spark Packages Repo" at "http://dl.bintray.com/spark-packages/maven",
  Resolver.bintrayRepo("hseeberger", "maven")
)

val circeVersion = "0.8.0"

val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  // AKKA
  "com.typesafe.akka" %% "akka-http" % "10.0.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10" % "test",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
  // Spark
  "org.apache.spark" % "spark-core_2.11" % "2.2.0",
  "org.apache.spark" % "spark-mllib_2.11" % "2.2.0",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5",
  // Neo4J
  "neo4j-contrib" % "neo4j-spark-connector" % "2.1.0-M4",
  "org.neo4j.driver" % "neo4j-java-driver" % "1.4.5",
  "ch.qos.logback" % "logback-classic" % "1.1.6",
  "de.heikoseeberger" %% "akka-http-circe" % "1.18.1",
  "com.jason-goodwin" %% "authentikat-jwt"     % "0.4.0"
)

libraryDependencies ++= circeDependencies

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "hello"
  )

mainClass in Compile := Some("co.com.gamerecommender.startup.Startup")

enablePlugins(JavaServerAppPackaging)

javaOptions in reStart := Seq(
  "-Dspark.neo4j.bolt.password=davidvadmin"
)

packageName in Universal := name.value

parallelExecution in Test := false
fork in Test := false

        
