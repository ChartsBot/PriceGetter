package com.chartsbot

import com.chartsbot.models.sql.{ SqlBlock, SqlBlocksPolygonDAO }
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, ExecutionContext }

class SqlTest extends AnyFeatureSpecLike with Matchers {

  val Injector: InjectorHelper = new InjectorHelper(List(new Binder {
  })) {}

  implicit val ec: ExecutionContext = Injector.get[ExecutionContext]

  Feature("Getting stuff") {

    val sqlBlocksPolygonDAO = Injector.get[SqlBlocksPolygonDAO]

    Scenario("Getting one block on polygon") {
      val ts = 1632925297
      val fRes = sqlBlocksPolygonDAO.getClosest(ts)
      val res = Await.result(fRes, 10.seconds)
      res.isRight shouldBe true
      res.right.get.size shouldBe 1
      res.right.get.head.blockTimestamp shouldBe ts
      println(res)
    }

    Scenario("Getting another block on polygon") {
      val ts = 1622925297
      val fRes = sqlBlocksPolygonDAO.getClosest(ts)
      val res = Await.result(fRes, 10.seconds)
      res.isRight shouldBe true
      res.right.get.size shouldBe 1
      println(res)
    }
  }

}
