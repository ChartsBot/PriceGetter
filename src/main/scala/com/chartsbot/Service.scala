package com.chartsbot

import com.chartsbot.services.Web3Connector

import java.util.concurrent.{ ScheduledThreadPoolExecutor, TimeUnit }
import javax.inject.{ Inject, Singleton }
import scala.collection.JavaConverters._

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {

    val environmentVars = System.getenv().asScala
    val r = for ((k, v) <- environmentVars) yield s"envVar: $k, value: $v"
    logger.info(r.toList.mkString(", "))

    get[ChartsBotWebserverApi].start()
  }

}

@Singleton
class ChartsBotWebserverApi @Inject() (jettyServer: DefaultJettyServer) {
  def start(): Unit = {
    jettyServer.start()
  }
}

@Singleton
class ReconnectWeb3IfNeeded @Inject() (web3Connector: Web3Connector) {
  def run(): Unit = {
    val ex = new ScheduledThreadPoolExecutor(1)
    val task = new Runnable {
      def run() = {
        ""
      }
    }
    val f = ex.scheduleAtFixedRate(task, 60, 30, TimeUnit.SECONDS)
    f.cancel(false)
  }
}
