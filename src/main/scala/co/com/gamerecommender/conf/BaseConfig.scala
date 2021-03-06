package co.com.gamerecommender.conf

import com.typesafe.config.{ Config, ConfigFactory }

object BaseConfig {

  val conf: Config = ConfigFactory.load()

  val sparkMaster = conf.getString("spark.master")

  val recomLimit: Int = conf.getInt("recommender.recommendations-limit")

  val neo4jHost: String = conf.getString("spark.neo4j.bolt.url")
  val neo4jUser: String = conf.getString("spark.neo4j.bolt.user")
  val neo4jPass: String = conf.getString("spark.neo4j.bolt.password")

}
