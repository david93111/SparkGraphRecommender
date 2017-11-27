package co.com.gamerecommender.api

import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ ALS, MatrixFactorizationModel, Rating }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.neo4j.spark.Neo4j

import scala.concurrent.ExecutionContext

trait SparkServices {

  implicit val executionContext: ExecutionContext

  val sparkContext: SparkContext
  val neoSpark: Neo4j
  val model: MatrixFactorizationModel

  def calculateALS(): Unit = {

    val data = sparkContext.textFile("src/main/resources/test.data.txt")
    val ratings: RDD[Rating] = data.map(_.split(',') match {
      case Array(user, item, rate) =>
        Rating(user.toInt, item.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 10
    val numIterations = 10
    val regLambda = 0.01
    val model = ALS.train(ratings, rank, numIterations, regLambda)

    // Evaluate the model on rating data
    val usersProducts = ratings.map {
      case Rating(user, product, rate) =>
        (user, product)
    }
    val predictions =
      model.predict(usersProducts).map {
        case Rating(user, product, rate) =>
          ((user, product), rate)
      }
    val ratesAndPreds = ratings.map {
      case Rating(user, product, rate) =>
        ((user, product), rate)
    }.join(predictions)
    val MSE = ratesAndPreds.map {
      case ((user, product), (r1, r2)) =>
        val err = (r1 - r2)
        err * err
    }
    println("Mean Squared Error = " + MSE.mean())

    // Save and load model
    //model.save(sparkContext, "target/tmp/myCollaborativeFilter")
    //val sameModel = MatrixFactorizationModel.load(sparkContext, "target/tmp/myCollaborativeFilter")
    //sameModel
  }

  def testProcessNeo4j(): Unit = {
    val rowRDD: RDD[Row] = neoSpark.cypher("MATCH (n:Person) RETURN n.name as name limit 5").loadRowRdd
    val a: Unit = rowRDD.map(t => "Name: " + t(0)).collect.foreach(println)
  }

  def getRecomendedProducts(): Unit = {
    val recommendedGames = model.recommendProducts(5, 10)
    recommendedGames.map(a => println(a))
    println(recommendedGames)

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
