package co.com.gamerecommender.model

import org.neo4j.driver.v1.{ Record, Value }

case class User(
  username: String,
  pass: String,
  name: String,
  country: String,
  age: Int,
  gender: String,
  id: Long) extends ApiModel

object User {
  def apply(record: Record): User = {
    val node: Value = record.get(0)
    User(
      node.get("username").asString(),
      node.get("pass").asString(),
      node.get("name").asString(),
      node.get("country").asString(),
      node.get("age").asInt(),
      node.get("genre").asString(),
      record.get("id").asLong())
  }
}
