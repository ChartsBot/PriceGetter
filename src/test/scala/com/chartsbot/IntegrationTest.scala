package com.chartsbot

import com.chartsbot.controlers.PriceRetrieverController
import com.chartsbot.models.SupportedChains
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext }

class IntegrationTest extends AnyFeatureSpecLike with Matchers with LazyLogging {

  val Injector: InjectorHelper = new InjectorHelper(List(new Binder {
  })) {}

  implicit val ec: ExecutionContext = Injector.get[ExecutionContext]

  Feature("Sql with web3") {

    val wbtc2Addy = "0x1bfd67037b42cf73acf2047067bd4f2c47d9bfd6"
    val chain = SupportedChains.Polygon

    val priceRetrieverController = Injector.get[PriceRetrieverController]

    Scenario("One data point please") {

      val ts = 1632925297

      val shouldBeBlock = 19646288

      val fRes = priceRetrieverController.handleTimestampBasedRequest(List(ts), wbtc2Addy)(chain)
      val res = Await.result(fRes, 10.seconds)
      res.size shouldBe 1
      res.head.isRight shouldBe true
      res.head.right.get.blockNumb shouldBe shouldBeBlock
      res.head.right.get.ts shouldBe ts
      println(res.head.right.get)

    }

    Scenario("A lot of data blz") {
      val tss = (1624442857 to 1632932383).filter(p => p % 100000 == 0).toList
      println(tss.size)
      val fRes = priceRetrieverController.handleTimestampBasedRequest(tss, wbtc2Addy)(chain)
      val res = Await.result(fRes, 10.seconds)
      //      res.size shouldBe 50
      for (r <- res) {
        r match {
          case Left(value) => println(value)
          case Right(value) => println(value)
        }
      }

    }

    Scenario("Stress test") {
      for (i <- 0 to 10) {
        val tss = (1624442857 + i * 11 to 1632932383 + i * 23).filter(p => p % 100000 == 0).toList
        println(tss.size)
        val fRes = priceRetrieverController.handleTimestampBasedRequest(tss, wbtc2Addy)(chain)
        val res = Await.result(fRes, 10.seconds)
        //      res.size shouldBe 50
        for (r <- res) {
          r match {
            case Left(value) => println(value)
            case Right(value) => println(value)
          }
        }
      }

    }

  }

}
