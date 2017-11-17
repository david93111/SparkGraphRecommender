package co.com.gamerecommender.startup

import co.com.gamerecommender.api.Services
import org.apache.spark.{ SparkConf, SparkContext }

object Startup extends Services {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("CollaborativeFilteringExample").setMaster("local[2]").set("spark.executor.memory", "1g")
    val sc = new SparkContext(conf)
    //calculateALS(sc)
    sc.stop()

  }

}
