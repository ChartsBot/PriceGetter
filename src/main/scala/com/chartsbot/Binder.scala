package com.chartsbot

import com.chartsbot.controlers.{ DefaultPriceRetrieverController, PriceRetrieverController }
import com.chartsbot.models.sql.{ DefaultSqlBlocksDAO, DefaultSqlPricesDAO, SqlBlocksDAO, SqlPricesDAO }
import com.chartsbot.models.web3.{ DefaultOracleDAO, OracleDAO }
import com.chartsbot.services.{ ConfigProvider, DefaultMySQLConnector, DefaultWeb3Connector, ExecutionProvider, MySQLConnector, Web3Connector }
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.{ AbstractModule, Module }
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class Binder extends AbstractModule {

  def Config: ScopedBindingBuilder = bind(classOf[Config]).toProvider(classOf[ConfigProvider])
  def ExecutionContext: ScopedBindingBuilder = bind(classOf[ExecutionContext]).toProvider(classOf[ExecutionProvider])
  def Web3Connector: ScopedBindingBuilder = bind(classOf[Web3Connector]).to(classOf[DefaultWeb3Connector])
  def OracleDAO: ScopedBindingBuilder = bind(classOf[OracleDAO]).to(classOf[DefaultOracleDAO])
  def PriceRetrieverController: ScopedBindingBuilder = bind(classOf[PriceRetrieverController]).to(classOf[DefaultPriceRetrieverController])
  def SqlBlocksPolygonDAO: ScopedBindingBuilder = bind(classOf[SqlBlocksDAO]).to(classOf[DefaultSqlBlocksDAO])
  def MySQLConnector: ScopedBindingBuilder = bind(classOf[MySQLConnector]).to(classOf[DefaultMySQLConnector])
  def SqlPricesDAO: ScopedBindingBuilder = bind(classOf[SqlPricesDAO]).to(classOf[DefaultSqlPricesDAO])

  override def configure(): Unit = {
    Config
    ExecutionContext
    Web3Connector
    OracleDAO
    PriceRetrieverController
    SqlBlocksPolygonDAO
    MySQLConnector
    SqlPricesDAO
  }

}

object Binder {
  def modules: List[Module] = List(new Binder)
}
