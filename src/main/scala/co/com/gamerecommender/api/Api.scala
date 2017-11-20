package co.com.gamerecommender.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import scala.concurrent.{ ExecutionContext, Future }

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
          onSuccess(getPing) { currentDeploy =>
            complete(OK, currentDeploy)
          }
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
