package com.chartsbot.models.sql

import com.chartsbot.models.{ PriceAtBlock, PriceAtTimestamp }

/**
  * Entity class storing rows of table TokenInfoRow
  *
  *  @param blockNumber Database column block_number SqlType(BIGINT), PrimaryKey
  *  @param timestamp Database column timestamp SqlType(BIGINT)
  *  @param priceUsdDecimaled Database column price_usd_decimaled SqlType(FLOAT), Default(0.0)
  */
case class TokenPricesRow(blockNumber: Long, timestamp: Long, priceUsdDecimaled: Double = 0.0) {
  def toPriceAtTimestamp: PriceAtTimestamp = {
    PriceAtTimestamp(blockNumb = blockNumber.toInt, ts = timestamp.toInt, price = Some(BigInt.long2bigInt((priceUsdDecimaled * math.pow(10, 18)).toLong)))
  }

  def toPriceAtBlock: PriceAtBlock = {
    PriceAtBlock(blockNumb = blockNumber.toInt, price = Some(BigInt.long2bigInt((priceUsdDecimaled * math.pow(10, 18)).toLong)))
  }
}
