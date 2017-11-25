package co.com.gamerecommender.api

import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ ALS, Rating }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.neo4j.spark.Neo4j

import scala.concurrent.ExecutionContext

trait Services {

  implicit val executionContext: ExecutionContext

  val sparkContext: SparkContext
  val neo: Neo4j

  def calculateALS(): Unit = {

    val data = sparkContext.textFile("src/main/resources/test.data.txt")
    val ratings = data.map(_.split(',') match {
      case Array(user, item, rate) =>
        Rating(user.toInt, item.toInt, rate.toDouble)
    })

    // Build the recommendation model using ALS
    val rank = 10
    val numIterations = 10
    val model = ALS.train(ratings, rank, numIterations, 0.01)

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

  def processNeo4j(): Unit = {
    val rowRDD: RDD[Row] = neo.cypher("MATCH (n:Person) RETURN n.name as name limit 5").loadRowRdd
    rowRDD.map(t => "Name: " + t(0)).collect.foreach(println)
  }

}
