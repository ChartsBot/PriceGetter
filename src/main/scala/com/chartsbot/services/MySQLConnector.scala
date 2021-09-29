package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.SqlPaths
import com.typesafe.config.{ Config, ConfigFactory }
import io.getquill._

import java.net.URI
import javax.inject.{ Inject, Singleton }
import scala.collection.JavaConverters._

trait MySQLConnector {

  val ctx: MysqlAsyncContext[CamelCase.type]

}

@Singleton
class DefaultMySQLConnector @Inject() (config: Config) extends MySQLConnector with SqlPaths {

  val uri = new URI(config.getString(MYSQL_CONNECTION_PATH))

  val cfg: Config = ConfigFactory.parseMap(Map(
    "dataSourceClassName" -> "dataSourceClassName=org.mariadb.jdbc.MariaDbDataSource",
    "url" -> config.getString(MYSQL_CONNECTION_PATH),
    "user" -> config.getString(MYSQL_CONNECTION_USERNAME),
    "password" -> config.getString(MYSQL_CONNECTION_PASSWORD),
    "cachePrepStmts" -> "true",
    "prepStmtCacheSize" -> 250,
    "prepStmtCacheSqlLimit" -> 2048,
    "connectionTimeout" -> 30000,
    "poolMaxObjects" -> 32,
    "poolMaxQueueSize" -> 1024
  ).asJava)

  val conf: MysqlAsyncContextConfig = MysqlAsyncContextConfig(cfg)

  lazy val ctx: MysqlAsyncContext[CamelCase.type] = new MysqlAsyncContext(CamelCase, conf)

}
