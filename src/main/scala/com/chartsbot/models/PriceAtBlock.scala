package com.chartsbot.models

import com.chartsbot.models.sql.TokenPricesRow

case class PriceAtBlock(blockNumb: Int, price: Option[BigInt])

case class PriceAtTimestamp(blockNumb: Int, ts: Int, price: Option[BigInt]) {
  def toMaybeTokenPricesRow: Option[TokenPricesRow] = {
    price match {
      case Some(_) => Some(TokenPricesRow(blockNumb.toLong, ts.toLong, (BigDecimal(price.get) / BigDecimal.long2bigDecimal(math.pow(10, 18).toLong)).toFloat))
      case None => None
    }
  }
}
