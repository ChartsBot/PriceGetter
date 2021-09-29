package com.chartsbot.models

case class PriceAtBlock(blockNumb: Int, price: BigInt)

case class PriceAtTimestamp(blockNumb: Int, ts: Int, price: BigInt)
