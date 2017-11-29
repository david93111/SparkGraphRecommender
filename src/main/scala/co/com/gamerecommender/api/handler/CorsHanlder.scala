package co.com.gamerecommender.api.handler

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive0, Route }
import co.com.gamerecommender.conf.BaseConfig

trait CorsHanlder {

  lazy val allowedOrigin: HttpOrigin = {
    val allowedOrigin = BaseConfig.conf.getString("recommender.cors.allowed-origins")
    HttpOrigin(allowedOrigin)
  }

  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`(allowedOrigin),
      `Access-Control-Allow-Credentials`(true),
      `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With"))
  }

  //this handles preflight OPTIONS requests.
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
  }

  def corsHandler(route: Route): Route = addAccessControlHeaders { route ~ preflightRequestHandler }

}
