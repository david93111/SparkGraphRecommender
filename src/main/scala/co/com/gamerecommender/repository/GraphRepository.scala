package co.com.gamerecommender.repository

import co.com.gamerecommender.conf.BaseConfig
import org.neo4j.driver.v1._

trait GraphRepository {

  val neoDriver: Driver

  def allGamesWithRating()

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
}