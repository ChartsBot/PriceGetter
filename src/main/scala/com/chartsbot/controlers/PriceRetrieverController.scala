package com.chartsbot.controlers

import com.chartsbot.models.{ PriceAtBlock, PriceAtTimestamp }
import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.models.sql.{ SqlBlocksEthDAO, SqlBlocksPolygonDAO }
import com.chartsbot.models.web3.OracleDAO
import com.chartsbot.services.Web3Connector
import com.typesafe.scalalogging.LazyLogging

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

trait PriceRetrieverController {

  def handleTimestampBasedRequest(timestamps: List[Int], token: String)(chain: SupportedChains): Future[List[Either[String, PriceAtTimestamp]]]

  def handleBlockNumberBaseRequest(blockNumbers: List[Int], token: String)(chain: SupportedChains): Future[Either[String, List[PriceAtBlock]]]

}

@Singleton
class DefaultPriceRetrieverController @Inject() (oracleDAO: OracleDAO, sqlBlocksPolygonDAO: SqlBlocksPolygonDAO, implicit val ec: ExecutionContext) extends PriceRetrieverController with LazyLogging {

  override def handleTimestampBasedRequest(timestamps: List[Int], token: String)(chain: SupportedChains): Future[List[Either[String, PriceAtTimestamp]]] = {

    val selectedDao = chain match {
      case com.chartsbot.models.SupportedChains.Polygon => sqlBlocksPolygonDAO
    }

    val a = for (timestamp <- timestamps) yield {
      val r = selectedDao.getClosest(timestamp).map {
        case Left(value) =>
          Future.successful(Left(s"Error getting closest block for timestamp ${timestamp}: ${value.errorMessage}."))
        case Right(block) =>
          logger.debug(s"Getting data for block: ${block.head.number} at ts ${block.head.blockTimestamp}")
          oracleDAO.getPriceAsync(token, block.head.number)(chain).map(p => Right(PriceAtTimestamp(p.blockNumb, timestamp, p.price)))
      }
      r.flatten

    }

    Future.sequence(a)

  }

  override def handleBlockNumberBaseRequest(blockNumbers: List[Int], token: String)(chain: SupportedChains): Future[Either[String, List[PriceAtBlock]]] = {

    oracleDAO.getPricesAsync(token, blockNumbers)(chain).map(Right(_))

  }

}
