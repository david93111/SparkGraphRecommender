package co.com.gamerecommender.api

import akka.actor.ActorRef
import akka.pattern.ask
import co.com.gamerecommender.actor.RecommenderActor.{ RecommendGamesForUser, TrainModel }
import co.com.gamerecommender.model.Game

import scala.concurrent.Future

trait RecommenderServices extends Services {

  val recommenderActor: ActorRef

  def getRecommendationsForUser(userId: Int): Future[Seq[Game]] = {
    val result = recommenderActor ? RecommendGamesForUser(userId)
    result.mapTo[Seq[Game]]
  }

  def trainRecommender(): Unit = {
    recommenderActor ! TrainModel
  }

}
