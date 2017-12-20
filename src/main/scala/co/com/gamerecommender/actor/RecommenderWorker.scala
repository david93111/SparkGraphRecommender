package co.com.gamerecommender.actor

import akka.actor.Props
import co.com.gamerecommender.actor.RecommenderActor.EnableAutoTrain
import co.com.gamerecommender.actor.RecommenderWorker.{ CalcGameRate, RecommendGamesUser }
import co.com.gamerecommender.conf.BaseConfig
import co.com.gamerecommender.model.Game
import co.com.gamerecommender.repository.GraphRepository
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

class RecommenderWorker() extends BaseActor {

  override def receive = {
    case RecommendGamesUser(user, model) =>
      sender() ! getRecomendedProductsForUser(user, model)
    case CalcGameRate(id) =>
      val rate = GraphRepository.getGameAverageRating(id)
      GraphRepository.updateGameRate(id, rate)
      sender() ! EnableAutoTrain
  }

  def getRecomendedProductsForUser(userId: Int, modelOpt: Option[MatrixFactorizationModel]): Seq[Game] = {
    modelOpt.fold {
      Seq.empty[Game]
    } { model =>
      val recommendedGames = model.recommendProducts(userId, BaseConfig.recomLimit)
      val products = recommendedGames.map(a => a.product.toLong).toSeq
      val games = GraphRepository.getGamesIn(products)
      games.sortBy(-_.rate)
    }
  }

}

object RecommenderWorker {

  case class RecommendGamesUser(userId: Int, model: Option[MatrixFactorizationModel])
  case class CalcGameRate(gameId: Long)

  def props() = {
    Props(new RecommenderWorker())
  }

}
