package co.com.gamerecommender.api

import co.com.gamerecommender.repository.GraphRepository

trait GraphServices extends Services {

  def testNeoDriver(): String = {
    val result = GraphRepository.queryTest
    println("The result is " + result)
    result
  }

}
