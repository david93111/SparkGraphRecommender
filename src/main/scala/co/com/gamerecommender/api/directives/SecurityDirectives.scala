package co.com.gamerecommender.api.directives

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.StatusCodes
import authentikat.jwt.{ JsonWebToken, JwtClaimsSet, JwtHeader }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import co.com.gamerecommender.api.codec.SecurityCodecs
import co.com.gamerecommender.model.{ User, UserAuth }
import co.com.gamerecommender.repository.GraphRepository
// Dont delete if is seen as unused, is required for circe codec over akka http
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

// Authorization JWT based, thanks to Branislav Lazic
trait SecurityDirectives extends SecurityCodecs {

  private val tokenExpiryPeriodInDays = 1
  private val secretKey = "you_shall_not_guess_this_but_if_you_guess_it_be_kind"
  private val header = JwtHeader("HS256")

  protected def login: Route = {
    entity(as[UserAuth]) { userAuth =>
      val user: Option[User] = GraphRepository.getUserByUserName(userAuth.username)
      user.fold[Route](
        complete(StatusCodes.Unauthorized, "Authentication failed, User not found")) { usr =>
          if (validateUser(usr, userAuth.username, userAuth.password)) {
            val claims = setClaims(userAuth.username, usr.id, tokenExpiryPeriodInDays)
            respondWithHeader(RawHeader("X-Auth-Token", JsonWebToken(header, claims, secretKey))) {
              complete(StatusCodes.OK, "Token generated")
            }
          } else {
            complete(StatusCodes.Unauthorized, "Authentication failed, Invalid Credentials")
          }
        }
    }
  }

  private def validateUser(user: User, username: String, pass: String): Boolean = {
    (user.username == username) && (user.pass == pass)
  }

  private def setClaims(username: String, userId: Long, expiryPeriodInDays: Long) = {
    JwtClaimsSet(
      Map(
        "user" -> username,
        "userID" -> userId,
        "expiresAt" -> (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expiryPeriodInDays))))
  }

  protected def authenticated: Directive1[Map[String, Any]] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) if isTokenExpired(jwt) =>
        complete(StatusCodes.Unauthorized -> "Token expired or invalid")
      case Some(jwt) if JsonWebToken.validate(jwt, secretKey) =>
        provide(getClaims(jwt).getOrElse(Map.empty[String, Any]))
      case _ =>
        complete(StatusCodes.Unauthorized -> "Authentication invalid, or none where provided")
    }

  protected def obtainUserName(map: Map[String, Any]): Directive1[String] = {
    val user = map.get("user")
    user match {
      case Some(username: String) => provide(username)
      case _ =>
        complete(StatusCodes.NotFound, "Could not retrieve user information for token")
    }
  }

  protected def obtainUserId(map: Map[String, Any]): Directive1[Int] = {
    val user = map.get("userID")
    user match {
      case Some(userId: String) => provide(userId.toInt)
      case _ =>
        complete(StatusCodes.NotFound, "Could not retrieve user information for token")
    }
  }

  private def isTokenExpired(jwt: String): Boolean = getClaims(jwt) match {
    case Some(claims) =>
      claims.get("expiresAt") match {
        case Some(value) => value.toLong < System.currentTimeMillis()
        case None => false
      }
    case None => false
  }

  private def getClaims(jwt: String): Option[Map[String, String]] = jwt match {
    case JsonWebToken(_, claims, _) => claims.asSimpleMap.toOption
    case _ => None
  }

}
