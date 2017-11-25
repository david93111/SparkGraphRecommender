package co.com.gamerecommender.api

import co.com.gamerecommender.repository.GraphRepository

trait GraphServices {

  def testNeoDriver(): String = {
    val result = GraphRepository.queryTest
    println("The result is " + result)
    result
  }


}
