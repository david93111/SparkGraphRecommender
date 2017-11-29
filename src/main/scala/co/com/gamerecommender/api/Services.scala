package co.com.gamerecommender.api

import co.com.gamerecommender.api.codec.DerivationCodec

import scala.concurrent.ExecutionContext

trait Services extends DerivationCodec {
  implicit val executionContext: ExecutionContext
}
