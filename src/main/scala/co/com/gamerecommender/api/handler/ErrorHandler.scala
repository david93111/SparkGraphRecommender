package co.com.gamerecommender.api.handler

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ complete, handleExceptions }
import akka.http.scaladsl.server.{ Directive0, ExceptionHandler }

trait ErrorHandler {

  val exceptionHanlder = ExceptionHandler {
    case e: Exception =>
      complete(StatusCodes.InternalServerError -> s"Unexpected error trying to fulfill the operation, Cause: ${e}")
  }

  def errorHandler: Directive0 = handleExceptions(exceptionHanlder)

}
