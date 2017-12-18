package co.com.gamerecommender.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import co.com.gamerecommender.api.directives.SecurityDirectives
import co.com.gamerecommender.api.handler.Handlers
import co.com.gamerecommender.model.{ GameLikeRequest, GameRateRequest }
// Dont delete if is seen as unused, is required for circe codec over akka http
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.Future

trait Api extends SecurityDirectives with Handlers with RecommenderServices with GraphServices {

  val apiRoute: Route = errorHandler {
    pathPrefix("recommender") {
      corsHandler {
        path("ping") {
          pathEndOrSingleSlash {
            get {
              onSuccess(getPing) { currentDeploy =>
                complete(OK, currentDeploy)
              }
            }
          }
        } ~ path("authenticate") {
          post {
            login
          } ~ get {
            authenticated { auth =>
              obtainUserName(auth) { user =>
                complete(OK, s"Token is active and belongs to user $user")
              }
            }
          }
        } ~ path("train") {
          get {
            authenticated { auth =>
              trainRecommender()
              complete(OK, "Training Started")
            }
          }
        } ~ pathPrefix("recommend") {
          get {
            authenticated { auth =>
              path("games") {
                obtainUserId(auth) { user =>
                  onSuccess(getRecommendationsForUser(user)) { games =>
                    complete(OK, games)
                  }
                }
              } ~ path("byprofile") {
                obtainUserName(auth) { user =>
                  onSuccess(recommendedGamesByProfile(user)) { games =>
                    complete(OK, games)
                  }
                }
              }
            }
          }
        } ~ pathPrefix("games") {
          authenticated { auth =>
            pathPrefix("relation") {
              obtainUserName(auth) { user =>
                path("like") {
                  post {
                    entity(as[GameLikeRequest]) { gameRequest =>
                      onSuccess(giveLikeToGame(user, gameRequest)) { res =>
                        complete(OK, res)
                      }
                    }
                  }
                } ~ path("rate") {
                  post {
                    entity(as[GameRateRequest]) { gameRequest =>
                      onSuccess(giveRateToGame(user, gameRequest)) { res =>
                        complete(OK, res)
                      }
                    }
                  }
                }
              }
            } ~ pathEndOrSingleSlash {
              get {
                parameters('limit.as[Int] ? 25, 'skip.as[Int] ? 0) { (limit, skip) =>
                  onSuccess(getAllGames(skip, limit)) { games =>
                    complete(OK, games)
                  }
                }
              }
            }
          }
        } ~ pathPrefix("users") {
          path("current") {
            get {
              authenticated { auth =>
                obtainUserName(auth) { username =>
                  onSuccess(getUserInfoByUsername(username)) { user =>
                    complete(OK, user)
                  }
                }
              }
            }
          }
        } ~ path("testNeo4j") {
          pathEndOrSingleSlash {
            get {
              authenticated { _ =>
                testNeoDriver()
                complete(OK, "OK")
              }
            }
          }
        }
      }
    }
  }

  private def getPing = Future {
    s"Status OK - version: ${BuildInfo.version}"
  }
}
