package com.chartsbot.models.sql

import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.services.MySQLConnector
import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.github.mauricio.async.db.mysql.message.server.ErrorMessage
import com.typesafe.scalalogging.LazyLogging
import io.getquill.{ CamelCase, MirrorSqlDialect, MysqlAsyncContext, Ord, Query, SqlMirrorContext }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SqlBlocksQueries {

  /**
    *
    * @return The last indexed block on the database
    */
  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]]

  /**
    * Get the block whose timestamp is the closest to the given ts
    */
  def getClosest(ts: Int): Future[Either[ErrorMessage, List[SqlBlock]]]

  /**
    * Dl the whole table
    */
  def getTable: Future[Either[ErrorMessage, List[SqlBlock]]]

  /**
    * Returns all rows whose ts are between from and to
    * @param from Lower bound timestamp
    * @param to Upper bound timestamp
    */
  def getRangeOfTs(from: Int, to: Int): Future[Either[ErrorMessage, List[SqlBlock]]]
}

object SqlQueriesUtil extends LazyLogging {
  /**
    * Simple helper that makes working with Quill easier
    * @param future Future on which to operate
    * @param errorLog Error to append in case of an error
    * @param ec Execution context
    * @tparam T Type of what's in the future
    * @return A transformed future.
    */
  def transformFuture[T](future: Future[Right[Nothing, T]], errorLog: String)(implicit ec: ExecutionContext): Future[Either[ErrorMessage, T]] = {

    future transformWith {
      case Success(value) => Future.successful(value)
      case Failure(exception) =>
        exception match {
          case e: MySQLException =>
            logger.info(errorLog + e.errorMessage.toString)
            Future.successful(Left(e.errorMessage))
        }
    }
  }
}

@Singleton
class SqlBlocksEthQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlBlocksQueries with LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  // TODO: find a way to make Quill work with dynamic entity names, would avoid creating a new class for each table.
  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("EthBlocks")

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error getting last block ")
  }

  def getClosest(ts: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val abs = quote {
      (a: Int) => infix"ABS($a)".as[Int]
    }

    val closestQuery = quote(query[SqlBlock].sortBy(b => abs(b.blockTimestamp - lift(ts))).take(1))
    val f = run(closestQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting closest block to ts:$ts")
  }

  def getTable: Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val tableQuery = quote(query[SqlBlock])
    val f = run(tableQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting whole table")
  }

  def getRangeOfTs(from: Int, to: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val rangeQuery = quote(query[SqlBlock].filter(_.blockTimestamp >= lift(from)).filter(_.blockTimestamp <= lift(to)))
    val f = run(rangeQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting range of ts from $from to $to")
  }

}

@Singleton
class SqlBlocksPolygonQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlBlocksQueries with LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("PolygonBlocks")

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error getting last block ")
  }

  def getClosest(ts: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {

    logger.debug(s"SqlPolygon getting block closest to ts $ts")

    val abs = quote {
      (a: Int) => infix"ABS($a)".as[Int]
    }

    val closestQuery = quote(query[SqlBlock].sortBy(b => abs(b.blockTimestamp - lift(ts))).take(1))
    val f = run(closestQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting closest block to ts:$ts")
  }

  def getTable: Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val tableQuery = quote(query[SqlBlock])
    val f = run(tableQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting whole table")
  }

  def getRangeOfTs(from: Int, to: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val rangeQuery = quote(query[SqlBlock].filter(_.blockTimestamp >= lift(from)).filter(_.blockTimestamp <= lift(to)))
    val f = run(rangeQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting range of ts from $from to $to")
  }

}

@Singleton
class SqlBlocksBscQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlBlocksQueries with LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("BscBlocks")

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error getting last block ")
  }

  def getClosest(ts: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val abs = quote {
      (a: Int) => infix"ABS($a)".as[Int]
    }

    val closestQuery = quote(query[SqlBlock].sortBy(b => abs(b.blockTimestamp - lift(ts))).take(1))
    val f = run(closestQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting closest block to ts:$ts")
  }

  def getTable: Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val tableQuery = quote(query[SqlBlock])
    val f = run(tableQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting whole table")
  }

  def getRangeOfTs(from: Int, to: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val rangeQuery = quote(query[SqlBlock].filter(_.blockTimestamp >= lift(from)).filter(_.blockTimestamp <= lift(to)))
    val f = run(rangeQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting range of ts from $from to $to")
  }

}

@Singleton
class SqlBlocksFtmQueries @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends SqlBlocksQueries with LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlBlock] = schemaMeta[SqlBlock]("FtmBlocks")

  def getLast: Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val maxQuery = quote(query[SqlBlock].sortBy(b => b.number)(Ord.descNullsLast).take(1))

    val f = run(maxQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, "SQL error getting last block ")
  }

  def getClosest(ts: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {

    val abs = quote {
      (a: Int) => infix"ABS($a)".as[Int]
    }

    val closestQuery = quote(query[SqlBlock].sortBy(b => abs(b.blockTimestamp - lift(ts))).take(1))
    val f = run(closestQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting closest block to ts:$ts")
  }

  def getTable: Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val tableQuery = quote(query[SqlBlock])
    val f = run(tableQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting whole table")
  }

  def getRangeOfTs(from: Int, to: Int): Future[Either[ErrorMessage, List[SqlBlock]]] = {
    val rangeQuery = quote(query[SqlBlock].filter(_.blockTimestamp >= lift(from)).filter(_.blockTimestamp <= lift(to)))
    val f = run(rangeQuery).map(Right(_))
    SqlQueriesUtil.transformFuture(f, s"SQL error getting range of ts from $from to $to")
  }

}

trait SqlBlocksDAO {

  def getLastBlock(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]]

  def getClosest(ts: Int)(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]]

  def getTable(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]]

  def getRangeOfTs(from: Int, to: Int)(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]]
}

@Singleton
class DefaultSqlBlocksDAO @Inject() (val sqlBlocksEthQueries: SqlBlocksEthQueries, val sqlBlocksPolygonQueries: SqlBlocksPolygonQueries, val sqlBlocksBscQueries: SqlBlocksBscQueries, val sqlBlocksFtmQueries: SqlBlocksFtmQueries, implicit val ec: ExecutionContext) extends SqlBlocksDAO with LazyLogging {

  private def selectChainSqlQueries(chain: SupportedChains): SqlBlocksQueries = {
    chain match {
      case com.chartsbot.models.SupportedChains.Polygon => sqlBlocksPolygonQueries
      case com.chartsbot.models.SupportedChains.Bsc => sqlBlocksBscQueries
      case com.chartsbot.models.SupportedChains.Eth => sqlBlocksEthQueries
      case com.chartsbot.models.SupportedChains.Ftm => sqlBlocksFtmQueries
    }
  }

  def getLastBlock(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]] = selectChainSqlQueries(chain).getLast

  def getClosest(ts: Int)(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]] = selectChainSqlQueries(chain).getClosest(ts)

  def getTable(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]] = selectChainSqlQueries(chain).getTable

  def getRangeOfTs(from: Int, to: Int)(chain: SupportedChains): Future[Either[ErrorMessage, List[SqlBlock]]] = selectChainSqlQueries(chain).getRangeOfTs(from, to)
}
