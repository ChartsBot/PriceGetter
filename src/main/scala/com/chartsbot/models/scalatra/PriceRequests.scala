package com.chartsbot.models.scalatra

trait PriceRequest {
  val address: String
}

case class PriceRequestsTimestamp()

case class PriceRequestsBlockNumber()
