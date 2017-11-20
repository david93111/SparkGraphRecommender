package co.com.gamerecommender.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import scala.concurrent.Future

trait Api extends Services {

  def apiRoute: Route = pathPrefix("recommender") {
    path("ping") {
      pathEndOrSingleSlash {
        get {
          onSuccess(getPing) { currentDeploy =>
            complete(OK, currentDeploy)
          }

        }
      }
    } ~ path("calculateALS") {
      pathEndOrSingleSlash {
        get {
          calculateALS
          complete(OK, "OK")

        }
      }
    } ~ path("testNeo4j") {
      pathEndOrSingleSlash {
        get {
          processNeo4j()
          complete(OK, "OK")

        }
      }
    }
  }

  private def getPing = Future {
    val p = getClass.getPackage
    val version = p.getImplementationVersion
    s"Status OK - version: $version"
  }
}
