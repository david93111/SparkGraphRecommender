package co.com.gamerecommender.repository

import co.com.gamerecommender.conf.BaseConfig
import co.com.gamerecommender.model.Game
import org.neo4j.driver.v1._

import scala.collection.JavaConverters._

trait GraphRepository {

  val neoDriver: Driver

  def allGamesWithRating()

  def getAllRatings()

  def gameWithRating(gameId: Int)

  def getGamesIn(gamesIds: Seq[Long]): Seq[Game]

  protected def executeReadTx[T](query: Statement, applyFun: (StatementResult) => T): T = {
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
    val session = neoDriver.session()
    val result: T = session.writeTransaction(new TransactionWork[T]() {
      override def execute(transaction: Transaction): T = {
        val stResult: StatementResult = transaction.run(query)
        applyFun(stResult)
      }
    })
    result
  }

  protected def executeQuery(query: Statement): StatementResult = {
    val session = neoDriver.session()
    val result = session.run(query)
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

  def getGamesIn(gamesIds: Seq[Long]): Seq[Game] = {
    val params = Map[String, Object]("gamesIds" -> gamesIds.asJava).asJava
    val statement = new Statement(
      "MATCH (g:GAME) WHERE id(g) IN {gamesIds} RETURN g.name as name, g.rate as rate,g.company as company, g.year as year",
      params)
    val result = executeQuery(statement)
    val resultList: Seq[Record] = result.list().asScala
    val games = resultList.map(Game(_))
    games
  }

  override def allGamesWithRating(): Unit = {

  }

  override def gameWithRating(gameId: Int): Unit = {

  }

  override def getAllRatings(): Unit = {
    val statement = new Statement(
      """
        |MATCH (u:USER)-[r:RATES]->(g:GAME)
        |return u.username as user,r.rate as rating,g.name as game
      """.stripMargin)
    val func = (r: StatementResult) => {
      val record: Seq[Record] = r.list().asScala

    }
  }
}