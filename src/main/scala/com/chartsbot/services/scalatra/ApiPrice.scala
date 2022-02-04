package com.chartsbot.services.scalatra

import com.typesafe.scalalogging.LazyLogging
import org.json4s.{ DefaultFormats, Formats }
import org.scalatra._
import org.scalatra.json.NativeJsonSupport
import org.web3j.crypto.WalletUtils._

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext
import com.chartsbot.Util.createServerError
import com.chartsbot.controlers.PriceRetrieverController
import com.chartsbot.models.SupportedChains.SupportedChains
import com.chartsbot.models.{ Elements, SupportedChains }

import scala.util.{ Failure, Success, Try }

@Singleton
class ApiPrice @Inject() (priceRetrieverController: PriceRetrieverController, implicit val ec: ExecutionContext) extends ScalatraServlet
  with FutureSupport with NativeJsonSupport with LazyLogging {

  // Sets up automatic case class to JSON output serialization
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  override protected implicit def executor: ExecutionContext = ec

  before() {
    contentType = formats("json")
  }

  def paramsToString: String = {
    params.toList.map(a => a._1 + " -> " + a._2).toString()
  }

  // post /polygon/prices?address=fjeihgr&timeType=timestamp
  post("/polygon") {
    logger.info(s"Request /api/v1/prices/polygon/$paramsToString")
    val chain = SupportedChains.Polygon
    handleQuery(chain)
  }

  post("/bsc") {
    logger.info(s"Request /api/v1/prices/bsc/$paramsToString")
    val chain = SupportedChains.Bsc
    handleQuery(chain)
  }

  post("/eth") {
    logger.info(s"Request /api/v1/prices/eth/$paramsToString")
    val chain = SupportedChains.Eth
    handleQuery(chain)
  }

  post("/ftm") {
    logger.info(s"Request /api/v1/prices/eth/$paramsToString")
    val chain = SupportedChains.Ftm
    handleQuery(chain)
  }

  /**
    * Handle the GET query.
    * GET query should have a timeType and an address
    * Calls the method corresponding to the chain given in input.
    */
  def handleQuery(chain: SupportedChains): Object = {
    val optionTimeType = params.get("timeType")
    val optionAddress = params.get("address")
    val optionWithHistory = params.get("history") match {
      case Some(value) => value.toLowerCase() match {
        case "true" => true
        case _ => false
      }
      case None => false
    }
    val maybeTimes = Try(request.body.split(",").map(_.toInt).toList)
    maybeTimes match {
      case Failure(_) =>
        val error = createServerError(Elements.WRONG_DEF_ERROR, s"Body should be a list of integer comma separated like (1243,243,1), not ${request.body}")
        BadRequest(error)
      case Success(times) =>
        optionAddress match {
          case Some(maybeValidAddress) =>
            if (isValidAddress(maybeValidAddress)) {
              optionTimeType match {
                case Some(value) =>
                  value.toLowerCase match {
                    case "timestamp" =>
                      priceRetrieverController.handleTimestampBasedRequest(times, maybeValidAddress, optionWithHistory)(chain)
                    case "blocknumbers" =>
                      priceRetrieverController.handleBlockNumberBaseRequest(times, maybeValidAddress, optionWithHistory)(chain)
                    case _ =>
                      val serverError = createServerError(Elements.WRONG_DEF_ERROR, s"timeType argument should either be timestamp or blocknumbers, not $value")
                      BadRequest(serverError)
                  }
                case None => // defaulting to timestamp
                  val serverError = createServerError(Elements.WRONG_DEF_ERROR, s"timeType argument should either be timestamp or blocknumbers, not nothing")
                  BadRequest(serverError)
              }
            } else {
              val error = createServerError(Elements.WRONG_DEF_ERROR, s"Address $maybeValidAddress is not a valid eth address. Please check https://github.com/ethereum/go-ethereum/issues/3205")
              BadRequest(error)
            }
          case None =>
            val error = createServerError(Elements.WRONG_DEF_ERROR, s"An address should be given as a path parameter such as address=...")
            BadRequest(error)
        }

    }
  }

}
