package co.com.gamerecommender.api

import akka.util.Timeout
import co.com.gamerecommender.api.codec.DerivationCodec

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{ Duration, FiniteDuration }

trait Services extends DerivationCodec {
  implicit val executionContext: ExecutionContext

  val d = Duration("10s")
  implicit val timeout: Timeout = Timeout(FiniteDuration(d.length, d.unit))

}
