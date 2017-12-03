package co.com.gamerecommender.api.codec

import co.com.gamerecommender.model.{ Game, User }
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._

trait DerivationCodec {

  // cleaner automatic derivation can be used, with lower performance as price
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]
  implicit val gameEncoder: Encoder[Game] = deriveEncoder[Game]

  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]

}

