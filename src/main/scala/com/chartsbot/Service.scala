package com.chartsbot

import javax.inject.{ Inject, Singleton }
import scala.collection.JavaConverters._

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {

    val environmentVars = System.getenv().asScala
    for ((k, v) <- environmentVars) println(s"envVar: $k, value: $v")

    get[ChartsBotWebserverApi].start()
  }

}

@Singleton
class ChartsBotWebserverApi @Inject() (jettyServer: DefaultJettyServer) {
  def start(): Unit = {
    jettyServer.start()
  }
}
