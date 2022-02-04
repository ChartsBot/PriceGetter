package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.SqlPaths
import com.typesafe.config.{ Config, ConfigFactory }
import io.getquill._
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import java.net.URI
import javax.inject.{ Inject, Singleton }
import scala.collection.JavaConverters._

trait MySQLConnector {

  val ctx: MysqlAsyncContext[CamelCase.type]
  val slickDbConnectionPriceDb: JdbcBackend.Database

}

@Singleton
class DefaultMySQLConnector @Inject() (config: Config) extends MySQLConnector with SqlPaths {

  val uri = new URI(config.getString(MYSQL_CONNECTION_PATH_BLOCK_INDEXER))

  val cfg: Config = ConfigFactory.parseMap(Map(
    "dataSourceClassName" -> "dataSourceClassName=org.mariadb.jdbc.MariaDbDataSource",
    "url" -> config.getString(MYSQL_CONNECTION_PATH_BLOCK_INDEXER),
    "user" -> config.getString(MYSQL_CONNECTION_USERNAME_BLOCK_INDEXER),
    "password" -> config.getString(MYSQL_CONNECTION_PASSWORD_BLOCK_INDEXER),
    "cachePrepStmts" -> "true",
    "prepStmtCacheSize" -> 250,
    "prepStmtCacheSqlLimit" -> 2048,
    "connectionTimeout" -> 30000,
    "poolMaxObjects" -> 32,
    "poolMaxQueueSize" -> 1024
  ).asJava)

  val conf: MysqlAsyncContextConfig = MysqlAsyncContextConfig(cfg)

  lazy val ctx: MysqlAsyncContext[CamelCase.type] = new MysqlAsyncContext(CamelCase, conf)

  val slickConfigPriceDb: Config = ConfigFactory.parseMap(Map(
    "driver" -> "com.mysql.cj.jdbc.Driver",
    "url" -> config.getString(MYSQL_CONNECTION_PATH_PRICE_DB),
    "user" -> config.getString(MYSQL_CONNECTION_USERNAME_PRICE_DB),
    "password" -> config.getString(MYSQL_CONNECTION_PASSWORD_PRICE_DB),
    "numThreads" -> config.getInt("ec.thread-number"),
    "queueSize" -> 1000
  ).asJava)

  //  val dbConfig: DatabaseConfig[slick.jdbc.MySQLProfile] = DatabaseConfig.forConfig(path = "", config = slickConfigPriceDb)
  //  val stuff: JdbcBackend#DatabaseDef = dbConfig.db

  val slickDbConnectionPriceDb: JdbcBackend.Database = Database.forConfig(path = "", config = slickConfigPriceDb)

}
