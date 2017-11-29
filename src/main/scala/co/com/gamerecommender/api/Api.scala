package co.com.gamerecommender.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
// Dont delete if is seen as unused, is required for codec entities derivation
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.Future

trait Api extends SparkServices with GraphServices {

  def apiRoute: Route = pathPrefix("recommender") {
    path("ping") {
      pathEndOrSingleSlash {
        get {
          onSuccess(getPing) { currentDeploy =>
            complete(OK, currentDeploy)
          }

        }
      }
    } ~ path("games") {
      get {
        onSuccess(getRecomendedProductsForUser(5)) { games =>
          complete(OK, games)
        }
      }
    } ~ path("testNeo4j") {
      pathEndOrSingleSlash {
        get {
          testProcessNeo4j()
          testNeoDriver()
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
