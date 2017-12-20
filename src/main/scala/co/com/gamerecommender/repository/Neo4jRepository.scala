package co.com.gamerecommender.repository

import org.neo4j.driver.v1._

trait Neo4jRepository {

  val neoDriver: Driver

  protected def executeReadTx[T](query: Statement, applyFun: (StatementResult) => T): T = {
    val session = neoDriver.session()
    val result: T = session.readTransaction(createTransactionWork[T](query, applyFun))
    result
  }

  protected def executeWriteTx[T](query: Statement, applyFun: (StatementResult) => T): T = {
    val session = neoDriver.session()
    val result: T = session.writeTransaction(createTransactionWork[T](query, applyFun))
    result
  }

  private def createTransactionWork[T](query: Statement, applyFun: (StatementResult) => T): TransactionWork[T] = {
    new TransactionWork[T]() {
      override def execute(transaction: Transaction): T = {
        val stResult: StatementResult = transaction.run(query)
        applyFun(stResult)
      }
    }
  }

  protected def executeQuery(query: Statement): StatementResult = {
    val session = neoDriver.session()
    val result = session.run(query)
    result
  }

}
