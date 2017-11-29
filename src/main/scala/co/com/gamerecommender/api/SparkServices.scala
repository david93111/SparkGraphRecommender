package co.com.gamerecommender.api

import co.com.gamerecommender.model.Game
import co.com.gamerecommender.repository.GraphRepository
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ ALS, MatrixFactorizationModel, Rating }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.neo4j.spark.Neo4j

import scala.concurrent.Future

trait SparkServices extends Services {

  val sparkContext: SparkContext
  val neoSpark: Neo4j
  val model: MatrixFactorizationModel

  def testProcessNeo4j(): Unit = {
    val rowRDD: RDD[Row] = neoSpark.cypher("MATCH (n:Person) RETURN n.name as name limit 5").loadRowRdd
    val a: Unit = rowRDD.map(t => "Name: " + t(0)).collect.foreach(println)
  }

  def getRecomendedProductsForUser(userId: Int): Future[Seq[Game]] = Future {
    val recommendedGames = model.recommendProducts(userId, 10)
    val products = recommendedGames.map(a => a.product.toLong)
    GraphRepository.getGamesIn(products.toSeq)
  }

  def trainModel(): MatrixFactorizationModel = {
    val rank = 10
    val numIterations = 10
    val ratings = getAllRatings()
    val model: MatrixFactorizationModel = ALS.train(ratings, rank, numIterations, 0.01)
    model
  }

  def getAllRatings(): RDD[Rating] = {
    val rowRDD: RDD[Row] = neoSpark.cypher(
      "MATCH (u:USER)-[r:RATES]->(g:GAME) return id(u) as user,id(g) as game,r.rate as rating").loadRowRdd
    val ratings: RDD[Rating] = rowRDD.map(row => Rating(row.getLong(0).toInt, row.getLong(1).toInt, row.getDouble(2)))
    ratings
  }

}
