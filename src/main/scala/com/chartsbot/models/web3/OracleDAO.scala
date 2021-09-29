package com.chartsbot.models.web3

import com.chartsbot.config.ConfigPaths.{Web3Paths, Web3PolygonPaths}
import com.chartsbot.contracts.OracleMainnet
import com.chartsbot.models.PriceAtBlock
import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.services.Web3Connector
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.web3j.crypto.{Credentials, WalletUtils}
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.tx.gas.DefaultGasProvider

import javax.inject.{Inject, Singleton}
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait OracleDAO {

  def getPricesAsync(tokenAddress: String, blocks: List[Int])(chain: SupportedChains): Future[List[PriceAtBlock]]

  def getPriceAsync(tokenAddress: String, block: Int)(chain: SupportedChains): Future[PriceAtBlock]

}

@Singleton
class DefaultOracleDAO @Inject() (web3Connector: Web3Connector, conf: Config, implicit val ec: ExecutionContext) extends OracleDAO with Web3PolygonPaths with Web3Paths with LazyLogging {

  val walletPwd: String = conf.getString(WEB3_PWD)
  val walletPath: String = conf.getString(WEB3_WALLET_PATH)

  val creds: Credentials = WalletUtils.loadCredentials(walletPwd, walletPath)

  private val gasProvider = new DefaultGasProvider()


  val polygonOracleContract: OracleMainnet = OracleMainnet.load(
    conf.getString(WEB3_POLYGON_ORACLE_ADDRESS),
    web3Connector.w3Polygon,
    creds,
    gasProvider
  )


  override def getPricesAsync(tokenAddress: String, blocks: List[Int])(chain: SupportedChains): Future[List[PriceAtBlock]] = {

    val res = blocks.map{block =>
      getPriceAsync(tokenAddress, block)(chain)
        .recoverWith {
          case e: Exception =>
            logger.error("Error getting price token async, retrying", e)
            getPriceAsync(tokenAddress, block)(chain)
        }}

    Future.sequence(res)

  }

  override def getPriceAsync(tokenAddress: String, block: Int)(chain: SupportedChains): Future[PriceAtBlock] = {

    val blockParam = new DefaultBlockParameterNumber(block)
    selectOracleBasedOnName(chain).setDefaultBlockParameter(blockParam)
    val fPriceBlock = selectOracleBasedOnName(chain).getPrice(tokenAddress).sendAsync().toScala.map(BigInt(_)).recoverWith{
      case e: Exception =>
        logger.error(s"Error getting price for $tokenAddress at block $block, recovering", e)
        selectOracleBasedOnName(chain).getPrice(tokenAddress).sendAsync().toScala.map(BigInt(_))
    }
    Thread.sleep(100) // Need some sleep to make sure that the block param is correctly set
    fPriceBlock.map(priceBlock => PriceAtBlock(block, priceBlock))

  }

  private def selectOracleBasedOnName(chain: SupportedChains): OracleMainnet = {
    chain match {
      case com.chartsbot.models.SupportedChains.Polygon => polygonOracleContract
    }
  }

}

