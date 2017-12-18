package co.com.gamerecommender.model

import co.com.gamerecommender.model.relation.{ RelationStatus, RelationType }

case class RelationResult(relationType: String, username: String, gameName: String, gameId: Long, operationDate: String, status: String)

object RelationResult {

  def apply(relationType: RelationType, username: String, gameName: String, gameId: Long, operationDate: String, status: RelationStatus): RelationResult = {
    RelationResult(relationType.toString, username, gameName, gameId, operationDate, status.toString)
  }
}
