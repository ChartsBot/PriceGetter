package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.{ Web3BscPaths, Web3EthPaths, Web3FtmPaths, Web3Paths, Web3PolygonPaths }
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
  val w3Ftm: Web3j
}

@Singleton
class DefaultWeb3Connector @Inject() (config: Config) extends Web3Connector with Web3Paths with Web3PolygonPaths with Web3BscPaths with Web3EthPaths with Web3FtmPaths with LazyLogging {

  private def setUpWebservice(wsConfPath: String): WebSocketService = {
    val urlWeb3ProviderChain = config.getString(wsConfPath)
    val webSocketChainClient = new CustomWebSocketClient(new URI(urlWeb3ProviderChain))
    new WebSocketService(webSocketChainClient, false)
  }

  val web3jPolygonService: WebSocketService = setUpWebservice(WEB3_POLYGON_CONNECTION_WS)
  web3jPolygonService.connect()

  val web3jBscService: WebSocketService = setUpWebservice(WEB3_BSC_CONNECTION_WS)
  web3jBscService.connect()

  val web3jEthService: WebSocketService = setUpWebservice(WEB3_ETH_CONNECTION_WS)
  web3jEthService.connect()

  val web3jFtmService: WebSocketService = setUpWebservice(WEB3_FTM_CONNECTION_WS)
  web3jFtmService.connect()

  val w3Polygon: Web3j = Web3j.build(web3jPolygonService)
  val w3Bsc: Web3j = Web3j.build(web3jBscService)
  val w3Eth: Web3j = Web3j.build(web3jEthService)
  val w3Ftm: Web3j = Web3j.build(web3jFtmService)

}
