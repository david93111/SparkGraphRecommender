package co.com.gamerecommender.repository

import co.com.gamerecommender.conf.BaseConfig
import org.neo4j.driver.v1._

trait GraphRepository {

  def allGamesWithRating()

  def gameWithRating(gameId: Int)

}

object GraphRepository extends GraphRepository{

  val neoDriver: Driver = GraphDatabase.driver(BaseConfig.neo4jHost, AuthTokens.basic(BaseConfig.neo4jUser, BaseConfig.neo4jPass))

  def queryTest: String = {
    val session = neoDriver.session()
    val result: String = session.readTransaction(new TransactionWork[String]() {
      override def execute(transaction: Transaction): String = {
        val result: StatementResult = transaction.run("MATCH (n:Person) RETURN n.name as name limit 1")
        result.single().get(0).asString()
      }
    })
    result
  }

  override def allGamesWithRating(): Unit = {

  }

  override def gameWithRating(gameId: Int): Unit = ???
}