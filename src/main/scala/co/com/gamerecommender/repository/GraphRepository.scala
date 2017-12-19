package co.com.gamerecommender.repository

import java.time.format.DateTimeFormatter
import java.time.{ Clock, Instant, ZoneId }

import co.com.gamerecommender.conf.BaseConfig
import co.com.gamerecommender.model.relation.{ RelationStatuses, RelationTypes }
import co.com.gamerecommender.model.{ Game, RelationResult, User }
import org.neo4j.driver.v1._

import scala.collection.JavaConverters._

sealed trait GraphRepository {

  val neoDriver: Driver

  def getGamesIn(gamesIds: Seq[Long]): Seq[Game]

  def getUserByUserName(username: String): Option[User]

  def getAllGamesWithLimit(skip: Int, limit: Int): Seq[Game]

  def recommendedGamesOfRelatedUsers(username: String): Seq[Game]

  def likeGame(username: String, gameId: Long): RelationResult

  def rateGame(username: String, gameId: Long, rate: Double): RelationResult

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
    val statement = new Statement("""MATCH(u:USER) return u,id(u) limit 1""")
    println("statement is ->" + statement)
    val queryResult = executeReadTx(statement, func)
    println("Node Value -> " + queryResult)

    queryResult.get(0).asMap().toString
  }

  def getGamesIn(gamesIds: Seq[Long]): Seq[Game] = {
    val params = Map[String, Object]("gamesIds" -> gamesIds.asJava)
    val statement = new Statement(
      "MATCH (g:GAME) WHERE id(g) IN {gamesIds} RETURN g as game, id(g) as id",
      params.asJava)
    val result = executeQuery(statement)
    val resultList: Seq[Record] = result.list().asScala
    val games = resultList.map(Game(_))
    games
  }

  override def getUserByUserName(username: String): Option[User] = {
    val params = Map[String, Object]("username" -> username)
    val statement = new Statement("MATCH (user:USER{username: {username} }) return user ,id(user) as id", params.asJava)
    val result = executeQuery(statement)
    val record = result.list().asScala.headOption
    record.map(User(_))
  }

  override def getAllGamesWithLimit(skip: Int, limit: Int): Seq[Game] = {
    val params = Map[String, Object]("limit" -> Int.box(limit), "skip" -> Int.box(skip))
    val dataReturn: String = "RETURN g as game, id(g) as id"
    val statement = new Statement(s"MATCH(g:GAME) $dataReturn SKIP {skip} LIMIT {limit}", params.asJava)
    val result = executeQuery(statement)
    val resultList: Seq[Record] = result.list().asScala
    resultList.map(Game(_))
  }

  override def recommendedGamesOfRelatedUsers(username: String): Seq[Game] = {
    val params = Map[String, Object](
      "username" -> username,
      "limit" -> Int.box(BaseConfig.recomLimit))
    val query: String = """MATCH(u:USER{username: {username} })
                             |MATCH(related:USER)-[rel:LIKES|RATES]->(g:GAME)
                             |WHERE (
                             |(related.age >= u.age - 4 AND related.age <= u.age +4 and related.country = u.country)
                             |OR (related.age >= u.age - 4 AND related.age <= u.age +4 and related.gender = u.gender)
                             |OR (related.gender = u.gender AND related.country = u.country)
                             |) AND g.rate > 3.8 AND u.username <> related.username
                             |RETURN distinct g as game, id(g) ORDER BY g.rate LIMIT {limit} """.stripMargin
    val statement = new Statement(query, params.asJava)
    val applyFuncToGames: StatementResult => Seq[Game] = (r: StatementResult) => {
      val resultList = r.list().asScala
      resultList.map(Game(_))
    }
    val result: Seq[Game] = executeReadTx(statement, applyFuncToGames)
    result
  }

  override def likeGame(username: String, gameId: Long): RelationResult = {
    val params = Map[String, Object](
      "username" -> username,
      "gameId" -> Long.box(gameId),
      "dateMilis" -> Long.box(System.currentTimeMillis()))
    val query =
      """MATCH(u:USER{username: {username} })
        |MATCH(g:GAME) where id(g) = {gameId}
        |MERGE (u)-[r:LIKES]->(g)
        |ON CREATE SET r.status = "CREATED"
        |ON MATCH SET r.status = "UPDATED"
        |SET r.dateMilis = {dateMilis}
        |RETURN r.dateMilis as milis,g.name as name, r.status as status
      """.stripMargin

    val statement = new Statement(query, params.asJava)

    val applyFuncToLike: StatementResult => RelationResult = (res: StatementResult) => {

      val defaultZone: ZoneId = Clock.systemDefaultZone().getZone

      val record: Record = res.single()

      val instant = Instant.ofEpochMilli(record.get("milis").asLong())

      val formatter = DateTimeFormatter.ISO_DATE

      RelationResult(
        RelationTypes.LIKE,
        username,
        record.get("name").asString(),
        gameId,
        formatter.format(instant.atZone(defaultZone)),
        record.get("status").asString())

    }

    val result: RelationResult = executeWriteTx(statement, applyFuncToLike)
    result

  }

  override def rateGame(username: String, gameId: Long, rate: Double): RelationResult = {
    val params = Map[String, Object](
      "username" -> username,
      "gameId" -> Long.box(gameId),
      "rate" -> Double.box(rate),
      "dateMilis" -> Long.box(System.currentTimeMillis()))

    val query =
      """MATCH(u:USER{username: {username} })
        |MATCH(g:GAME) where id(g) = {gameId}
        |MERGE (u)-[r:RATES]->(g)
        |ON CREATE SET r.status = "CREATED"
        |ON MATCH SET r.status = "UPDATED"
        |SET r.dateMilis = {dateMilis}
        |SET r.rate = {rate}
        |RETURN r.dateMilis as milis,g.name as name, r.status as status
      """.stripMargin

    val statement = new Statement(query, params.asJava)

    val applyFuncToLike: StatementResult => RelationResult = (res: StatementResult) => {

      val defaultZone: ZoneId = Clock.systemDefaultZone().getZone

      val record: Record = res.single()

      val instant = Instant.ofEpochMilli(record.get("milis").asLong())

      val formatter = DateTimeFormatter.ISO_DATE

      RelationResult(
        RelationTypes.RATE,
        username,
        record.get("name").asString(),
        gameId,
        formatter.format(instant.atZone(defaultZone)),
        record.get("status").asString())

    }

    val result: RelationResult = executeWriteTx(statement, applyFuncToLike)
    result

  }

}