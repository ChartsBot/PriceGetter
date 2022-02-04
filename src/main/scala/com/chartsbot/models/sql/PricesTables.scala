package com.chartsbot.models.sql

import com.chartsbot.models.SupportedChains.SupportedChains
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object PricesTables extends {
  val profile = slick.jdbc.MySQLProfile
} with PricesTables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait PricesTables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{ GetResult => GR }

  //  /** DDL for all tables. Call .create to execute. */
  //  lazy val schema: profile.SchemaDescription = TableInfo.schema ++ TokenInfo.schema
  //  @deprecated("Use .schema instead of .ddl", "3.0")
  //  def ddl = schema

  /**
    * Entity class storing rows of table TableInfo
    *  @param addy Database column addy SqlType(VARCHAR), PrimaryKey, Length(255,true), Default()
    *  @param ticker Database column ticker SqlType(VARCHAR), Length(255,true), Default(Some())
    *  @param name Database column name SqlType(VARCHAR), Length(255,true), Default(Some())
    */
  case class TableInfoRow(addy: String = "", ticker: Option[String] = Some(""), name: Option[String] = Some(""))
  /** GetResult implicit for fetching TableInfoRow objects using plain SQL queries */
  implicit def GetResultTableInfoRow(implicit e0: GR[String], e1: GR[Option[String]]): GR[TableInfoRow] = GR {
    prs =>
      import prs._
      TableInfoRow.tupled((<<[String], <<?[String], <<?[String]))
  }
  /** Table description of table table_info. Objects of this class serve as prototypes for rows in queries. */
  class TableInfo(_tableTag: Tag, chainName: SupportedChains) extends profile.api.Table[TableInfoRow](_tableTag, Some("prices"), f"${chainName.toString.toLowerCase()}_info") {
    def * = (addy, ticker, name) <> (TableInfoRow.tupled, TableInfoRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(addy), ticker, name).shaped.<>({ r => import r._; _1.map(_ => TableInfoRow.tupled((_1.get, _2, _3))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column addy SqlType(VARCHAR), PrimaryKey, Length(255,true), Default() */
    val addy: Rep[String] = column[String]("addy", O.PrimaryKey, O.Length(255, varying = true), O.Default(""))
    /** Database column ticker SqlType(VARCHAR), Length(255,true), Default(Some()) */
    val ticker: Rep[Option[String]] = column[Option[String]]("ticker", O.Length(255, varying = true), O.Default(Some("")))
    /** Database column name SqlType(VARCHAR), Length(255,true), Default(Some()) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Length(255, varying = true), O.Default(Some("")))
  }
  /** Collection-like TableQuery object for table TableInfo */
  //  lazy val TableInfo = new TableQuery((tag) => new TableInfo(tag, chainName))

  /** GetResult implicit for fetching TokenInfoRow objects using plain SQL queries */
  implicit def GetResultTokenInfoRow(implicit e0: GR[Long], e1: GR[Double]): GR[TokenPricesRow] = GR {
    prs =>
      import prs._
      TokenPricesRow.tupled((<<[Long], <<[Long], <<[Double]))
  }
  /** Table description of table TokenInfo. Objects of this class serve as prototypes for rows in queries. */
  class TokenPrices(_tableTag: Tag, chainName: SupportedChains, tokenAddress: String) extends profile.api.Table[TokenPricesRow](_tableTag, Some("prices"), f"${chainName.toString.toLowerCase()}_${tokenAddress.toLowerCase()}") {
    def * = (blockNumber, timestamp, priceUsdDecimaled) <> (TokenPricesRow.tupled, TokenPricesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(blockNumber), Rep.Some(timestamp), Rep.Some(priceUsdDecimaled)).shaped.<>({ r => import r._; _1.map(_ => TokenPricesRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column block_number SqlType(BIGINT), PrimaryKey */
    val blockNumber: Rep[Long] = column[Long]("block_number", O.PrimaryKey)
    /** Database column timestamp SqlType(BIGINT) */
    val timestamp: Rep[Long] = column[Long]("timestamp")
    /** Database column price_usd_decimaled SqlType(FLOAT), Default(0.0) */
    val priceUsdDecimaled: Rep[Double] = column[Double]("price_usd_decimaled", O.Default(0.0F))
  }
  /** Collection-like TableQuery object for table TokenInfo */
  //  lazy val TokenInfo = new TableQuery(tag => new TokenInfo(tag))

}
