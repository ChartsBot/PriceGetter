package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.{ Web3BscPaths, Web3EthPaths, Web3Paths, Web3PolygonPaths }
import com.chartsbot.util.CustomWebSocketClient
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.web3j.protocol.Web3j
import org.web3j.protocol.websocket.WebSocketService

import java.net.URI
import javax.inject.{ Inject, Singleton }

trait Web3Connector {
  val w3Polygon: Web3j
  val w3Bsc: Web3j
  val w3Eth: Web3j
}

@Singleton
class DefaultWeb3Connector @Inject() (config: Config) extends Web3Connector with Web3Paths with Web3PolygonPaths with Web3BscPaths with Web3EthPaths with LazyLogging {

  val urlWeb3ProviderPolygon: String = config.getString(WEB3_POLYGON_CONNECTION_WS)
  val urlWeb3ProviderBSC: String = config.getString(WEB3_BSC_CONNECTION_WS)
  val urlWeb3ProviderETH: String = config.getString(WEB3_ETH_CONNECTION_WS)

  val webSocketPolygonClient = new CustomWebSocketClient(new URI(urlWeb3ProviderPolygon))
  val webSocketBscClient = new CustomWebSocketClient(new URI(urlWeb3ProviderBSC))
  val webSocketEthClient = new CustomWebSocketClient(new URI(urlWeb3ProviderETH))

  val web3jPolygonService = new WebSocketService(webSocketPolygonClient, false)
  web3jPolygonService.connect()

  val web3jBscService = new WebSocketService(webSocketBscClient, false)
  web3jBscService.connect()

  val web3jEthService = new WebSocketService(webSocketEthClient, false)
  web3jEthService.connect()

  val w3Polygon: Web3j = Web3j.build(web3jPolygonService)
  val w3Bsc: Web3j = Web3j.build(web3jBscService)
  val w3Eth: Web3j = Web3j.build(web3jEthService)

}
