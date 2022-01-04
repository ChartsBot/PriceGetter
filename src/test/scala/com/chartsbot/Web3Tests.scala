package com.chartsbot

import com.chartsbot.models.{ PriceAtBlock, SupportedChains }
import com.chartsbot.models.web3.OracleDAO
import com.chartsbot.services.Web3Connector
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import org.web3j.protocol.core.DefaultBlockParameterName

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext, blocking }

class Web3Tests extends AnyFeatureSpecLike with Matchers with LazyLogging {

  val Injector: InjectorHelper = new InjectorHelper(List(new Binder {
  })) {}

  implicit val ec: ExecutionContext = Injector.get[ExecutionContext]

  Feature("Getting prices") {
    val chain = SupportedChains.Polygon

    val gfarm2Addy = "0x7075cab6bcca06613e2d071bd918d1a0241379e2"

    val oracle = Injector.get[OracleDAO]

    Scenario("Getting single price of a token") {
      val block = 19347918
      val fRes = oracle.getPriceAsync(gfarm2Addy, block)(chain)
      val res = Await.result(fRes, 10.seconds)
      println(res)
    }

    Scenario("Getting multiple price points of a token") {

      val blocks = (19347918 to 19447918).filter(p => p % 1000 == 0)

      val fRes = oracle.getPricesAsync(gfarm2Addy, blocks.toList)(chain)
      val res = Await.result(fRes, 10.seconds)
      println(res)

    }

    Scenario("Getting prices one by one should be the same as all together") {

      val blocks = (19347918 to 19447918).filter(p => p % 1000 == 0)

      logger.info("Starting getting synced results")
      val syncedRes = for (block <- blocks) yield {
        val fRes = oracle.getPriceAsync(gfarm2Addy, block)(chain)
        Await.result(fRes, 10.seconds)
      }
      logger.info("Done getting synced results")

      println(syncedRes.toList)
      logger.info("Starting getting asynced results")
      val fAsyncedResults = oracle.getPricesAsync(gfarm2Addy, blocks.toList)(chain)
      val asyncRes = Await.result(fAsyncedResults, 10.seconds)
      logger.info("Done getting asynced results")

      println(asyncRes)
      syncedRes.toList.sortWith(_.price < _.price) shouldBe asyncRes.sortWith(_.price < _.price)

    }

    Scenario("Just a test") {
      val block = 19656269
      val tokenAddy = "0x1bfd67037b42cf73acf2047067bd4f2c47d9bfd6"
      val fRes = oracle.getPriceAsync(tokenAddy, block)(chain)
      val res = Await.result(fRes, 10.seconds)
      res shouldBe PriceAtBlock(19656269, Some(BigInt("41021187869699527391278")))
    }

  }

}
