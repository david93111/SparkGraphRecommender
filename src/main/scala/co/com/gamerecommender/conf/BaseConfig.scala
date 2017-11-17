package co.com.gamerecommender.conf

import com.typesafe.config.{ Config, ConfigFactory }

object BaseConfig {

  val conf: Config = ConfigFactory.defaultApplication()

}
