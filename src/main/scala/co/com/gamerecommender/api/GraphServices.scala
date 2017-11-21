package co.com.gamerecommender.api

import co.com.gamerecommender.conf.BaseConfig
import org.neo4j.driver.v1._

trait GraphServices {

  val neoDriver: Driver = GraphDatabase.driver(BaseConfig.neo4jHost, AuthTokens.basic(BaseConfig.neo4jUser, BaseConfig.neo4jPass))

  println("is the driver working over encryption ? ---> " + neoDriver.isEncrypted)

  def testNeoDriver(): String = {
    val session = neoDriver.session()
    neoDriver
    val result: String = session.readTransaction(new TransactionWork[String]() {
      override def execute(transaction: Transaction): String = {
        val result: StatementResult = transaction.run("MATCH (n:Person) RETURN n.name as name limit 1")
        result.single().get(0).asString()
      }
    })
    println("The result is " + result)
    result
  }

}
