package co.com.gamerecommender.api

import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ ALS, Rating }

import scala.concurrent.ExecutionContext

trait Services {

  implicit val executionContext: ExecutionContext

  val sparkContext: SparkContext

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
    }.mean()
    println("Mean Squared Error = " + MSE)

    // Save and load model
    //model.save(sparkContext, "target/tmp/myCollaborativeFilter")
    //val sameModel = MatrixFactorizationModel.load(sparkContext, "target/tmp/myCollaborativeFilter")
    //sameModel
  }

  def processNeo4j(): Unit = {
    import org.neo4j.spark._
    val conf: Neo4jConfig = Neo4jConfig.apply("localhost:7687", "neo4j", Some("admin"))
    val neo = Neo4j(sparkContext)
    val rowRDD = neo.cypher("MATCH (n:Person) RETURN n.name as name limit 4").loadRowRdd
    rowRDD.map(t => "Name: " + t(0)).collect.foreach(println)
  }

}
