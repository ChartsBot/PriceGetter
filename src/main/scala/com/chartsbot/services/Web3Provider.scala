package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.{ Web3Paths, Web3PolygonPaths }
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.web3j.protocol.Web3j
import org.web3j.protocol.websocket.WebSocketService

import javax.inject.{ Inject, Singleton }

trait Web3Connector {
  val w3Polygon: Web3j
}

@Singleton
class DefaultWeb3Connector @Inject() (config: Config) extends Web3Connector with Web3Paths with Web3PolygonPaths with LazyLogging {

  val urlWeb3ProviderPolygon: String = config.getString(WEB3_POLYGON_CONNECTION_WS)

  val web3jPolygonService = new WebSocketService(urlWeb3ProviderPolygon, false)
  web3jPolygonService.connect()

  val w3Polygon: Web3j = Web3j.build(web3jPolygonService)

}
