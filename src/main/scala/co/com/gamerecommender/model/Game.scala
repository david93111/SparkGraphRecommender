package co.com.gamerecommender.model

import org.neo4j.driver.v1.Record

case class Game(name: String, company: String, year: String, rate: Double)

object Game {
  def apply(record: Record): Game = {
    Game(
      record.get("name").asString(),
      record.get("company").asString(),
      record.get("year").asString(),
      record.get("rate").asDouble())
  }
}
