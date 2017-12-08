package co.com.gamerecommender.actor

import akka.actor.Props
import co.com.gamerecommender.actor.RecommenderWorker.RecommendGamesUser
import co.com.gamerecommender.conf.BaseConfig
import co.com.gamerecommender.model.Game
import co.com.gamerecommender.repository.GraphRepository
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

class RecommenderWorker() extends BaseActor {

  override def receive = {
    case RecommendGamesUser(user, model) =>
      sender() ! getRecomendedProductsForUser(user, model)

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

  def props() = {
    Props(new RecommenderWorker())
  }

}
