package co.com.gamerecommender.startup

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import co.com.gamerecommender.conf.BaseConfig

object Context {

  val actorSystem: ActorSystem = ActorSystem("game-recommender-system", BaseConfig.conf)

  val defaultDispatcher: MessageDispatcher = actorSystem.dispatchers.lookup("dispatchers.core-dispatcher")

}
