package co.com.gamerecommender.api

import co.com.gamerecommender.model.{ ApiError, ApiModel, Game, User }
import co.com.gamerecommender.repository.GraphRepository

import scala.concurrent.Future

trait GraphServices extends Services {

  def testNeoDriver(): String = {
    val result = GraphRepository.queryTest
    println("The result is " + result)
    result
  }

  def getAllGames(skip: Int, limit: Int): Future[Seq[Game]] = Future {
    val result = GraphRepository.getAllGamesWithLimit(skip, limit)
    result
  }

  def getUserInfoByUsername(username: String): Future[Option[User]] = Future {
    val result = GraphRepository.getUserByUserName(username)
    result
  }

  def recommendedGamesByProfile(username: String): Future[Seq[Game]] = Future {
    val result = GraphRepository.recommendedGamesOfRelatedUsers(username)
    result
  }

}
