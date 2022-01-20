package com.chartsbot

import com.chartsbot.models.SupportedChains
import com.chartsbot.models.sql.{ SqlBlock, SqlBlocksDAO }
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.Success

class SqlTest extends AnyFeatureSpecLike with Matchers with LazyLogging {

  val Injector: InjectorHelper = new InjectorHelper(List(new Binder {
  })) {}

  implicit val ec: ExecutionContext = Injector.get[ExecutionContext]

  Feature("Getting stuff") {

    val chainPolygon = SupportedChains.Polygon
    val chainEth = SupportedChains.Eth

    val sqlBlocksDAO = Injector.get[SqlBlocksDAO]

    Scenario("Getting one block on polygon") {
      val ts = 1632925297
      val fRes = sqlBlocksDAO.getClosest(ts)(chainPolygon)
      val res = Await.result(fRes, 10.seconds)
      res.isRight shouldBe true
      res.right.get.size shouldBe 1
      res.right.get.head.blockTimestamp shouldBe ts
      println(res)
    }

    Scenario("Getting another block on polygon") {
      val ts = 1622925297
      val fRes = sqlBlocksDAO.getClosest(ts)(chainPolygon)
      val res = Await.result(fRes, 10.seconds)
      res.isRight shouldBe true
      res.right.get.size shouldBe 1
      println(res)
    }

    Scenario("Getting multiple blocks") {
      val tsStart = 1622925297
      val tsStop = 1632925297
      val tss = (tsStart to tsStop).filter(p => p % 100000 == 0)
      val t0 = System.currentTimeMillis()
      val fBlocks = for (ts <- tss) yield {
        val r = sqlBlocksDAO.getClosest(ts)(chainPolygon)
        r onComplete {
          case Success(value) => logger.debug(s"Got block $ts")
        }
        r
      }
      Await.result(Future.sequence(fBlocks), 100.seconds)
      val t1 = System.currentTimeMillis()
      println(s"Took ${t1 - t0} ms to fetch ${tss.size} blocks")
    }

    Scenario("Speed test") {
      val ts = System.currentTimeMillis() - 1000 * 60 * 24
      // warm up
      val r = sqlBlocksDAO.getClosest(ts.toInt - 500)(SupportedChains.Eth)
      Await.result(r, 100.seconds)
      for (aChain <- SupportedChains.values) {
        val t0 = System.currentTimeMillis()
        val r = sqlBlocksDAO.getClosest(ts.toInt)(aChain)
        Await.result(r, 100.seconds)
        val t1 = System.currentTimeMillis()
        println(s"${aChain} took ${t1 - t0} ms")
      }
    }

    Scenario("Get whole table") {
      val t0 = System.currentTimeMillis()
      logger.debug("dling whole table")
      val fT = sqlBlocksDAO.getTable(chainPolygon)
      val t = Await.result(fT, 100.seconds)
      val t1 = System.currentTimeMillis()
      println(s"Took ${t1 - t0} ms to dl whole table")

    }

    Scenario("Get all data from last 24 hours") {
      val t0 = System.currentTimeMillis()
      val today = t0 / 1000
      val yesterday = today - 3600 * 24
      logger.debug("dling last day")
      val fT = sqlBlocksDAO.getRangeOfTs(yesterday.toInt, today.toInt)(chainPolygon)
      val t = Await.result(fT, 100.seconds)
      val t1 = System.currentTimeMillis()
      println(s"Took ${t1 - t0} ms to dl last day data (${t.right.get.size} rows)")
    }

    Scenario("Get all data from last month") {
      val t0 = System.currentTimeMillis()
      val today = t0 / 1000
      val yesterday = today - 3600 * 24 * 7
      logger.debug("dling last month")
      val fT = sqlBlocksDAO.getRangeOfTs(yesterday.toInt, today.toInt)(chainPolygon)
      val t = Await.result(fT, 100.seconds)
      val t1 = System.currentTimeMillis()
      println(s"Took ${t1 - t0} ms to dl last month data (${t.right.get.size} rows)")
    }
  }

}
