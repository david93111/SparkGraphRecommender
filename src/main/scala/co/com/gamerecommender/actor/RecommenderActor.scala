package co.com.gamerecommender.actor

import java.time.LocalDateTime

import akka.actor.{ ActorRef, Cancellable, Props }
import akka.pattern.{ ask, pipe }
import akka.routing.FromConfig
import co.com.gamerecommender.actor.RecommenderActor._
import co.com.gamerecommender.actor.RecommenderWorker.{ CalcGameRate, RecommendGamesUser }
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ ALS, MatrixFactorizationModel, Rating }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.neo4j.spark.Neo4j

import scala.concurrent.duration._

class RecommenderActor(sparkContext: SparkContext) extends BaseActor {

  var model: Option[MatrixFactorizationModel] = None

  var needTraining: Boolean = true

  val neoSpark: Neo4j = Neo4j(sparkContext)

  val recommenderWorkers: ActorRef = context.actorOf(FromConfig.props(RecommenderWorker.props()), "recommendWorker")

  context.watch(recommenderWorkers)

  val autoTrainCancellable: Cancellable =
    context.system.scheduler.schedule(
      100 milliseconds,
      6 hours,
      self,
      AutoTrainModel)

  override def receive = {
    case TrainModel =>
      model = Some(trainModel())
      logger.info("Model trainend at {} ", LocalDateTime.now())
    case RecommendGamesForUser(user) =>
      val sendTo = sender()
      val result = recommenderWorkers ? RecommendGamesUser(user, model)
      result pipeTo sendTo
    case RecalculateGameRate(id) =>
      recommenderWorkers ! CalcGameRate(id)
    case AutoTrainModel =>
      if (needTraining) {
        model = Some(trainModel())
        needTraining = false
        logger.info("Model auto trained at {} ", LocalDateTime.now())
      }
    case EnableAutoTrain =>
      needTraining = true

  }

  private def trainModel(): MatrixFactorizationModel = {
    val rank = 10
    val numIterations = 10
    val ratings: RDD[Rating] = obtainAllRatings()
    val trainedModel: MatrixFactorizationModel = ALS.train(ratings, rank, numIterations, 0.01)
    trainedModel
  }

  private def obtainAllRatings(): RDD[Rating] = {
    val rowRDD: RDD[Row] = neoSpark.cypher(
      "MATCH (u:USER)-[r:RATES]->(g:GAME) return id(u) as user,id(g) as game,r.rate as rating").loadRowRdd
    println("row rdd" + rowRDD)
    val ratings: RDD[Rating] = rowRDD.map(row => Rating(row.getLong(0).toInt, row.getLong(1).toInt, row.getDouble(2)))
    ratings
  }
}

object RecommenderActor {

  case object TrainModel
  case class RecommendGamesForUser(userId: Int)
  case object AutoTrainModel
  case object EnableAutoTrain
  case class RecalculateGameRate(gameId: Long)

  def props(sparkContext: SparkContext): Props = {
    Props(new RecommenderActor(sparkContext))
  }

}
