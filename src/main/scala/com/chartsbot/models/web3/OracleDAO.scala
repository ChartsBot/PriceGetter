package com.chartsbot.models.web3

import com.chartsbot.config.ConfigPaths.{ Web3BscPaths, Web3EthPaths, Web3Paths, Web3PolygonPaths }
import com.chartsbot.contracts.OracleMainnet
import com.chartsbot.models.PriceAtBlock
import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.services.Web3Connector
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.web3j.crypto.{ Credentials, WalletUtils }
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.tx.exceptions.ContractCallException
import org.web3j.tx.gas.DefaultGasProvider
import retry.Success

import java.math.BigInteger
import javax.inject.{ Inject, Singleton }
import scala.compat.java8.FutureConverters.CompletionStageOps
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Failure

trait OracleDAO {

  def getPricesAsync(tokenAddress: String, blocks: List[Int])(chain: SupportedChains): Future[List[PriceAtBlock]]

  def getPriceAsync(tokenAddress: String, block: Int)(chain: SupportedChains): Future[PriceAtBlock]

}

@Singleton
class DefaultOracleDAO @Inject() (web3Connector: Web3Connector, conf: Config, implicit val ec: ExecutionContext) extends OracleDAO
  with Web3PolygonPaths with Web3BscPaths with Web3EthPaths with Web3Paths with LazyLogging {

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

  val bscOracleContract: OracleMainnet = OracleMainnet.load(
    conf.getString(WEB3_BSC_ORACLE_ADDRESS),
    web3Connector.w3Bsc,
    creds,
    gasProvider
  )

  val ethOracleContract: OracleMainnet = OracleMainnet.load(
    conf.getString(WEB3_ETH_ORACLE_ADDRESS),
    web3Connector.w3Eth,
    creds,
    gasProvider
  )

  override def getPricesAsync(tokenAddress: String, blocks: List[Int])(chain: SupportedChains): Future[List[PriceAtBlock]] = {

    val res = blocks.map { block =>
      getPriceAsync(tokenAddress, block)(chain)
        .recoverWith {
          case e: Exception =>
            logger.error("Error getting price token async, retrying", e)
            getPriceAsync(tokenAddress, block)(chain)
        }
    }

    Future.sequence(res)

  }

  override def getPriceAsync(tokenAddress: String, block: Int)(chain: SupportedChains): Future[PriceAtBlock] = synchronized {

    val blockParam = new DefaultBlockParameterNumber(block)
    selectOracleBasedOnName(chain).setDefaultBlockParameter(blockParam)
    val success = Success[BigInteger](_ != null)
    val fRetried = retry.Backoff(5, 50.milliseconds).apply(
      promise = selectOracleBasedOnName(chain).getPrice(tokenAddress).sendAsync().toScala.recoverWith {
        case _: ContractCallException =>
          logger.debug(s"Seems like token $tokenAddress on $chain didn't exist / had liq at the block $block.")
          Future.successful(BigInteger.ZERO)
        case e: Throwable =>
          logger.error(s"Error getting price for $tokenAddress at block $block, recovering", e)
          Future.failed(e)
      }
    )(success, ec)
    Thread.sleep(10) // Need some sleep to make sure that the block param is correctly set
    fRetried.map(priceBlock =>
      if (priceBlock == BigInteger.ZERO) PriceAtBlock(block, None) else PriceAtBlock(block, Some(priceBlock)))
  }

  private def selectOracleBasedOnName(chain: SupportedChains): OracleMainnet = {
    chain match {
      case com.chartsbot.models.SupportedChains.Polygon => polygonOracleContract
      case com.chartsbot.models.SupportedChains.Bsc => bscOracleContract
      case com.chartsbot.models.SupportedChains.Eth => ethOracleContract
    }
  }

}

