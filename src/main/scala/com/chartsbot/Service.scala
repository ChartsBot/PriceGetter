package com.chartsbot

import com.chartsbot.services.Web3Connector

import java.util.concurrent.{ ScheduledThreadPoolExecutor, TimeUnit }
import javax.inject.{ Inject, Singleton }
import scala.collection.JavaConverters._

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {

    val environmentVars = System.getenv().asScala
    val r = for ((k, v) <- environmentVars) yield s"envVar: $k, value: $v"
    logger.debug(r.toList.mkString(", "))

    get[ChartsBotWebserverApi].start()
  }

}

@Singleton
class ChartsBotWebserverApi @Inject() (jettyServer: DefaultJettyServer) {
  def start(): Unit = {
    jettyServer.start()
  }
}
