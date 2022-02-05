package com.chartsbot.controlers

import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.models.sql.{ SqlBlocksDAO, SqlPricesDAO }
import com.chartsbot.models.web3.OracleDAO
import com.chartsbot.models.{ PriceAtBlock, PriceAtTimestamp }
import com.typesafe.scalalogging.LazyLogging

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

trait PriceRetrieverController {

  /**
    * Functions that returns a list of PriceAtTimestamp values corresponding to at least the timestamps given as an
    * argument for the given token on the given chain.
    * The withHistory option will fetch all the stored historical value between the lowest and highest given timestamp.
    * The method will attempt to correspond the given timestamp to the closest block available on the blockchain.
    * @param timestamps List of timestamp in seconds.
    * @param token Address of the desired token.
    * @param withHistory If the method should fetch additional data in the database.
    * @param chain Chain on which the token is located.
    * @return A future of lists of results.
    */
  def handleTimestampBasedRequest(timestamps: List[Int], token: String, withHistory: Boolean)(chain: SupportedChains): Future[List[Either[String, PriceAtTimestamp]]]

  /**
    * Same as handleTimestampBasedRequest but with block numbers instead of timestamps.
    */
  def handleBlockNumberBaseRequest(blockNumbers: List[Int], token: String, withHistory: Boolean)(chain: SupportedChains): Future[List[PriceAtBlock]]

}

@Singleton
class DefaultPriceRetrieverController @Inject() (oracleDAO: OracleDAO, sqlBlocksDAO: SqlBlocksDAO, sqlPricesDAO: SqlPricesDAO, implicit val ec: ExecutionContext) extends PriceRetrieverController with LazyLogging {

  /**
    * Helper that stores prices in the designated database
    */
  def storeData(token: String, datas: Future[List[Either[String, PriceAtTimestamp]]])(chain: SupportedChains): Unit = {

    for {
      lR <- datas
    } {
      val rights = for { r <- lR } yield {
        r match {
          case Left(_) => None
          case Right(value) => value.toMaybeTokenPricesRow
        }
      }
      sqlPricesDAO.addValues(token.toLowerCase(), rights.flatten)(chain)
    }

  }

  override def handleTimestampBasedRequest(timestamps: List[Int], token: String, withHistory: Boolean)(chain: SupportedChains): Future[List[Either[String, PriceAtTimestamp]]] = {
    val lowerBoundTs = timestamps.min
    val upperBoundTs = timestamps.max

    val alreadyQueried: Future[List[Right[String, PriceAtTimestamp]]] = if (withHistory) {
      sqlPricesDAO.getRangeOfTs(tokenAddy = token, from = lowerBoundTs.toLong, to = upperBoundTs.toLong)(chain)
        .map(_
          .map(p => Right[String, PriceAtTimestamp](p.toPriceAtTimestamp)).toList)
    } else {
      Future.successful(List.empty)
    }

    val fR: Future[List[Either[String, PriceAtTimestamp]]] = if (timestamps.size >= 5) { // If querying many ts, getting all the blockNumbers from lowestTs to highestTs then iterating on them (faster than doing it server-side)

      val fRows = sqlBlocksDAO.getRangeOfTs(from = lowerBoundTs - 1, to = upperBoundTs + 1)(chain)
      val r: Future[Future[List[Either[String, PriceAtTimestamp]]]] = fRows map {
        case Left(value) =>
          val a = Future.successful(List(Left(s"Sql Error getting ts range for timestamp range $lowerBoundTs - $upperBoundTs: ${value.errorMessage}.")))
          a
        case Right(sqlBlocks) =>
          val r: List[Future[Right[Nothing, PriceAtTimestamp]]] = for (ts <- timestamps) yield {
            val closestBlock = sqlBlocks.minBy(p => math.abs(p.blockTimestamp - ts))
            logger.debug(s"Getting data for block: ${closestBlock.number} at ts $ts")
            oracleDAO.getPriceAsync(token, closestBlock.number)(chain).map(p => Right(PriceAtTimestamp(p.blockNumb, ts, p.price)))
          }
          Future.sequence(r)
      }
      r.flatten
    } else {

      val a = for (timestamp <- timestamps) yield {
        val r = sqlBlocksDAO.getClosest(timestamp)(chain).map {
          case Left(value) =>
            Future.successful(Left(s"Error getting closest block for timestamp $timestamp: ${value.errorMessage}."))
          case Right(block) =>
            logger.debug(s"Getting data for block: ${block.head.number} at ts ${block.head.blockTimestamp}")
            oracleDAO.getPriceAsync(token, block.head.number)(chain).map(p => Right(PriceAtTimestamp(p.blockNumb, timestamp, p.price)))
        }
        r.flatten

      }

      Future.sequence(a)
    }
    fR.onComplete(_ => logger.debug(s"finished getting values for size ts of ${timestamps.size} on chain $chain"))
    storeData(token, fR)(chain)

    for {
      f1Res <- alreadyQueried
      f2Res <- fR
    } yield f1Res ::: f2Res

  }

  override def handleBlockNumberBaseRequest(blockNumbers: List[Int], token: String, withHistory: Boolean)(chain: SupportedChains): Future[List[PriceAtBlock]] = {

    val lowerBoundTs = blockNumbers.min
    val upperBoundTs = blockNumbers.max

    val alreadyQueried: Future[List[PriceAtBlock]] = if (withHistory) {
      sqlPricesDAO.getRangeOfBlockNumber(tokenAddy = token, from = lowerBoundTs.toLong, to = upperBoundTs.toLong)(chain)
        .map(
          _.map(_.toPriceAtBlock).toList
        )
    } else {
      Future.successful(List.empty)
    }

    val fR: Future[List[PriceAtBlock]] = oracleDAO.getPricesAsync(token, blockNumbers)(chain)

    for {
      f1Res <- alreadyQueried
      f2Res <- fR
    } yield f1Res ::: f2Res

  }

}
