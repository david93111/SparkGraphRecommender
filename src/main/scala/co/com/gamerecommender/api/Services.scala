package co.com.gamerecommender.api

import scala.concurrent.ExecutionContext

trait Services {
  implicit val executionContext: ExecutionContext
}
