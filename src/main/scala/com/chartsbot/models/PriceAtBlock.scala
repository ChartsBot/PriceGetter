package com.chartsbot.models

case class PriceAtBlock(blockNumb: Int, price: Option[BigInt])

case class PriceAtTimestamp(blockNumb: Int, ts: Int, price: Option[BigInt])
