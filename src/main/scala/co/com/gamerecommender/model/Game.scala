package co.com.gamerecommender.model

import org.neo4j.driver.v1.{ Record, Value }

case class Game(name: String, company: String, year: String, rate: Double, id: Long)

object Game {
  def apply(record: Record): Game = {
    val node = record.get("game").asNode()
    Game(
      node.get("name").asString(),
      node.get("company").asString(),
      node.get("year").asString(),
      node.get("rate").asDouble(),
      record.get("id").asLong())
  }
}
