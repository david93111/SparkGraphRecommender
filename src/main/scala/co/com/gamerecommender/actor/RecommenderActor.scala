package co.com.gamerecommender.actor

import akka.actor.Actor
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

class RecommenderActor extends Actor {

  var model: Option[MatrixFactorizationModel] = None

  override def receive = {
    case _ =>
  }
}
