package com.chartsbot.models.sql

import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.services.MySQLConnector
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.MySQLProfile.api._

import javax.inject.{Inject, Singleton}
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds


trait SqlPricesDAO extends PricesTables {

  def getRangeOfTs(tokenAddy: String, from: Long, to: Long)(chain: SupportedChains): Future[Seq[TokenPricesRow]]

  def getRangeOfBlockNumber(tokenAddy: String, from: Long, to: Long)(chain: SupportedChains): Future[Seq[TokenPricesRow]]

  def addValues(tokenAddy: String, values: Seq[TokenPricesRow])(chain: SupportedChains): Future[Unit]

}

@Singleton
class DefaultSqlPricesDAO @Inject () (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlPricesDAO with LazyLogging {

  override val profile = slick.jdbc.MySQLProfile

  def getTokenInfo(tokenAddy: String)(chain: SupportedChains): TableQuery[TokenPrices] =
    TableQuery[TokenPrices]((t: slick.lifted.Tag) => new TokenPrices(t, chain, tokenAddy))


  def getRangeOfTs(tokenAddy: String, from: Long, to: Long)(chain: SupportedChains): Future[Seq[TokenPricesRow]] = {

    val db = sqlConnector.slickDbConnectionPriceDb
    val tokenPrices = getTokenInfo(tokenAddy)(chain)

    val q = for {
      p <- tokenPrices if p.timestamp <= to && p.timestamp >= from
    } yield p

    val r: Future[Seq[TokenPricesRow]] = db.run(q.result)
    r.onComplete(_ => logger.info("finished getting db data"))
    r


  }

  def getRangeOfBlockNumber(tokenAddy: String, from: Long, to: Long)(chain: SupportedChains): Future[Seq[TokenPricesRow]] = {

    val db = sqlConnector.slickDbConnectionPriceDb
    val tokenPrices = getTokenInfo(tokenAddy)(chain)

    val q = for {
      p <- tokenPrices if p.blockNumber <= to && p.blockNumber >= from
    } yield p

    val r: Future[Seq[TokenPricesRow]] = db.run(q.result)
    r.onComplete(_ => logger.info("finished getting db data"))
    r


  }

  def addValues(tokenAddy: String, values: Seq[TokenPricesRow])(chain: SupportedChains): Future[Unit] = {

    val db = sqlConnector.slickDbConnectionPriceDb
    val tokenPrices = getTokenInfo(tokenAddy)(chain)


    val addValuesAction = DBIO.seq(
      tokenPrices.schema.createIfNotExists,
      tokenPrices ++= values,
    )

    val r = db.run(addValuesAction)
    r.onComplete(_ => logger.info("finished storing data to db"))
    r

  }


}
