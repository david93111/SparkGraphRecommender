package co.com.gamerecommender.repository

import java.util

import co.com.gamerecommender.conf.BaseConfig
import org.apache.spark.mllib.recommendation.{ ALS, Rating }
import org.neo4j.driver.v1._

import collection.JavaConverters._

trait GraphRepository {

  val neoDriver: Driver

  def allGamesWithRating()

  def getAllRatings()

  def gameWithRating(gameId: Int)

  protected def executeReadTx[T](query: Statement, applyFun: (StatementResult) => T): T = {
    neoDriver.session()
    val session = neoDriver.session()
    val result: T = session.readTransaction(new TransactionWork[T]() {
      override def execute(transaction: Transaction): T = {
        val stResult: StatementResult = transaction.run(query)
        applyFun(stResult)
      }
    })
    result
  }

  protected def executeWriteTx[T](query: Statement, applyFun: (StatementResult) => T): T = {
    neoDriver.session()
    val session = neoDriver.session()
    val result: T = session.writeTransaction(new TransactionWork[T]() {
      override def execute(transaction: Transaction): T = {
        val stResult: StatementResult = transaction.run(query)
        applyFun(stResult)
      }
    })
    result
  }

}

object GraphRepository extends GraphRepository {

  private val authToken = AuthTokens.basic(BaseConfig.neo4jUser, BaseConfig.neo4jPass)
  val neoDriver: Driver = GraphDatabase.driver(BaseConfig.neo4jHost, authToken)

  def queryTest: String = {
    val func = (r: StatementResult) => {
      println("resultStatement -> " + r)
      val record = r.single()
      println("result row " + record)
      record.values()
    }
    val statement = new Statement("""MATCH(u:USER{username:"davidv"}) return u,id(u)""")
    println("statement is ->" + statement)
    val queryResult = executeReadTx(statement, func)
    println("Node Value -> " + queryResult)

    queryResult.get(0).asMap().toString
  }

  override def allGamesWithRating(): Unit = {

  }

  override def gameWithRating(gameId: Int): Unit = {

  }

  override def getAllRatings(): Unit = {
    val statement = new Statement(
      """
        |MATCH (u:USER)-[r:RATES]->(g:GAME)
        |return id(u) as user,r.rate as rating,id(g) as game
      """.stripMargin)
    val func = (r: StatementResult) => {
      val record: Seq[Record] = r.list().asScala

      val ratings: Seq[Rating] = record.map { row =>
        val map = row.asMap()
        Rating(row.get(0).asInt(), row.get(1).asInt(), row.get(2).asDouble())
      }

    }
  }
}