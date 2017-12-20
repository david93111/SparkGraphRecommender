package co.com.gamerecommender.api

import co.com.gamerecommender.actor.RecommenderActor.RecalculateGameRate
import co.com.gamerecommender.model._
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

  def giveLikeToGame(username: String, relationRequest: GameLikeRequest): Future[RelationResult] = Future {
    val result = GraphRepository.likeGame(username, relationRequest.gameId)
    result
  }

  def giveRateToGame(username: String, relationRequest: GameRateRequest): Future[RelationResult] = Future {
    val result = GraphRepository.rateGame(username, relationRequest.gameId, relationRequest.rate)
    recommenderActor ! RecalculateGameRate(relationRequest.gameId)
    result
  }

}
