package co.com.gamerecommender.api.codec

import co.com.gamerecommender.model._
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._

trait DerivationCodec {

  // cleaner automatic derivation can be used, with lower performance on compilation as price
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]
  implicit val gameEncoder: Encoder[Game] = deriveEncoder[Game]

  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]

  implicit val gameLikeDecoder: Decoder[GameLikeRequest] = deriveDecoder[GameLikeRequest]
  implicit val gameRateDecoder: Decoder[GameRateRequest] = deriveDecoder[GameRateRequest]

  implicit val relationResultDecoder: Encoder[RelationResult] = deriveEncoder[RelationResult]

}

