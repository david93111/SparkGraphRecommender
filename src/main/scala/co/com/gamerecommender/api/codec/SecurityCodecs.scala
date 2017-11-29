package co.com.gamerecommender.api.codec

import co.com.gamerecommender.model.UserAuth
import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }
trait SecurityCodecs {

  // cleaner automatic derivation can be used, with lower performance as price
  implicit val authDecoder: Decoder[UserAuth] = deriveDecoder[UserAuth]
  implicit val authEncoder: Encoder[UserAuth] = deriveEncoder[UserAuth]

}
