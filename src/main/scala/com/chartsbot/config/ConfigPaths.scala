package com.chartsbot.config

object ConfigPaths {

  trait EcPaths {
    final val EC_NUMBER_THREADS = "ec.thread-number"
  }

  trait ScalatraPaths {
    final val SCALATRA_ENV = "scalatra.env"
  }

  trait ServerPaths {
    final val SERVER_PORT = "scalatra.port"
    final val SERVER_BASE_URL = "scalatra.base_url"
    final val APP_VERSION = "scalatra.version"
  }

  trait Web3Paths {
    final val WEB3_PWD = "web3.wallet.pwd"
    final val WEB3_WALLET_PATH = "web3.wallet.path"
  }

  trait Web3PolygonPaths {
    final val WEB3_POLYGON_CONNECTION_URL = "web3.polygon.connection.url"
    final val WEB3_POLYGON_CONNECTION_WS = "web3.polygon.connection.websocketUrl"
    final val WEB3_POLYGON_ORACLE_ADDRESS = "web3.polygon.price-oracle.address"
  }

  trait Web3BscPaths {
    final val WEB3_BSC_CONNECTION_URL = "web3.bsc.connection.url"
    final val WEB3_BSC_CONNECTION_WS = "web3.bsc.connection.websocketUrl"
    final val WEB3_BSC_ORACLE_ADDRESS = "web3.bsc.price-oracle.address"
  }

  trait Web3EthPaths {
    final val WEB3_ETH_CONNECTION_URL = "web3.eth.connection.url"
    final val WEB3_ETH_CONNECTION_WS = "web3.eth.connection.websocketUrl"
    final val WEB3_ETH_ORACLE_ADDRESS = "web3.eth.price-oracle.address"
  }

  trait Web3FtmPaths {
    final val WEB3_FTM_CONNECTION_URL = "web3.ftm.connection.url"
    final val WEB3_FTM_CONNECTION_WS = "web3.ftm.connection.websocketUrl"
    final val WEB3_FTM_ORACLE_ADDRESS = "web3.ftm.price-oracle.address"
  }

  trait SqlPaths {
    final val MYSQL_CONNECTION_PATH_BLOCK_INDEXER = "sql.block-indexer.connection.path"
    final val MYSQL_CONNECTION_USERNAME_BLOCK_INDEXER = "sql.block-indexer.connection.username"
    final val MYSQL_CONNECTION_PASSWORD_BLOCK_INDEXER = "sql.block-indexer.connection.password"
    final val MYSQL_CONNECTION_PATH_PRICE_DB = "sql.price-db.connection.path"
    final val MYSQL_CONNECTION_USERNAME_PRICE_DB = "sql.price-db.connection.username"
    final val MYSQL_CONNECTION_PASSWORD_PRICE_DB = "sql.price-db.connection.password"
  }

}
