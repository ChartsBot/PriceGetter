package com.chartsbot.models.sql

import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.services.MySQLConnector
import slick.jdbc.MySQLProfile.api._

import javax.inject.{Inject, Singleton}
import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds


trait SqlPricesDAO extends PricesTables {

  def getRangeOfTs(tokenAddy: String, from: Long, to: Long)(chain: SupportedChains): Future[Seq[TokenPricesRow]]

  def getRangeOfBlockNumber(tokenAddy: String, from: Long, to: Long)(chain: SupportedChains): Future[Seq[TokenPricesRow]]

  def addValues(tokenAddy: String, values: Seq[TokenPricesRow])(chain: SupportedChains): Unit

}

@Singleton
class DefaultSqlPricesDAO @Inject () (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlPricesDAO {

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

    r


  }

  def getRangeOfBlockNumber(tokenAddy: String, from: Long, to: Long)(chain: SupportedChains): Future[Seq[TokenPricesRow]] = {

    val db = sqlConnector.slickDbConnectionPriceDb
    val tokenPrices = getTokenInfo(tokenAddy)(chain)

    val q = for {
      p <- tokenPrices if p.blockNumber <= to && p.blockNumber >= from
    } yield p

    val r: Future[Seq[TokenPricesRow]] = db.run(q.result)

    r


  }

  def addValues(tokenAddy: String, values: Seq[TokenPricesRow])(chain: SupportedChains): Unit = {

    val db = sqlConnector.slickDbConnectionPriceDb
    val tokenPrices = getTokenInfo(tokenAddy)(chain)


    val addValuesAction = DBIO.seq(
      tokenPrices.schema.createIfNotExists,
      tokenPrices ++= values,
    )

    db.run(addValuesAction)

  }


}
