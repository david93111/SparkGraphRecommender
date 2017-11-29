package co.com.gamerecommender.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import co.com.gamerecommender.api.directives.SecurityDirectives
// Dont delete if is seen as unused, is required for circe codec over akka http
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.Future

trait Api extends SecurityDirectives with SparkServices with GraphServices {

  def apiRoute: Route = pathPrefix("recommender") {
    path("ping") {
      pathEndOrSingleSlash {
        get {
          onSuccess(getPing) { currentDeploy =>
            complete(OK, currentDeploy)
          }
        }
      }
    } ~ path("authenticate") {
      login
    } ~ path("games") {
      get {
        authenticated { auth =>
          obtainUserId(auth) { user =>
            onSuccess(getRecomendedProductsForUser(user)) { games =>
              complete(OK, games)
            }
          }
        }
      }
    } ~ path("testNeo4j") {
      pathEndOrSingleSlash {
        get {
          authenticated { _ =>
            testProcessNeo4j()
            testNeoDriver()
            complete(OK, "OK")
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
