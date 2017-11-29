package co.com.gamerecommender.startup

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import co.com.gamerecommender.api.Api
import co.com.gamerecommender.conf.BaseConfig
import com.typesafe.config.Config
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.{ SparkConf, SparkContext }
import org.neo4j.spark.Neo4j

import scala.concurrent.duration.{ Duration, FiniteDuration }
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

object Startup {

  implicit def requestTimeout: Timeout = configuredRequestTimeout(BaseConfig.conf)
  implicit val system: ActorSystem = Context.actorSystem
  val log = Logging(system.eventStream, "akka_project")

  def main(args: Array[String]) {
    startUp
  }

  def startUp()(implicit system: ActorSystem) = {

    val conf = new SparkConf().setAppName("CollaborativeFilteringExample").setMaster("local[2]").set("spark.executor.memory", "1g")
    val sc: SparkContext = new SparkContext(conf)

    val api = new Api {

      implicit val executionContext: ExecutionContext = Context.defaultDispatcher

      val sparkContext: SparkContext = sc
      val neoSpark: Neo4j = Neo4j(sparkContext)

      val model: MatrixFactorizationModel = trainModel()
    }

    implicit val ec: ExecutionContextExecutor = system.dispatcher
    val host = system.settings.config.getString("http.host")
    val port = system.settings.config.getInt("http.port")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val bindingFuture: Future[ServerBinding] =
      Http().bindAndHandle(api.apiRoute, host, port)

    bindingFuture.map { serverBinding =>
      log.info(s"Server Started on ${serverBinding.localAddress} ")
      //log.info("the bolt URL is ->" + conf.get("spark.neo4j.bolt.url"))
      system.registerOnTermination {
        sc.stop()
      }
    }.onFailure {
      case ex: Exception =>
        log.error(ex, s"Server Bind Failed cause $ex", host, port)
        sc.stop()
        system.terminate()
    }

  }

  def configuredRequestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }

}
