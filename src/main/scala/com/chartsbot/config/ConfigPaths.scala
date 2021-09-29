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

  trait SqlPaths {
    final val MYSQL_CONNECTION_PATH = "sql.connection.path"
    final val MYSQL_CONNECTION_USERNAME = "sql.connection.username"
    final val MYSQL_CONNECTION_PASSWORD = "sql.connection.password"
  }

}
