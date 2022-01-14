package com.chartsbot.models

import com.chartsbot.models

/**
 * Enum of all supported blockchains.
 */
object SupportedChains extends Enumeration {

  type SupportedChains = Value

  val Polygon: models.SupportedChains.Value = Value
  val Bsc: models.SupportedChains.Value = Value
  val Eth: models.SupportedChains.Value = Value
  val Ftm: models.SupportedChains.Value = Value

}
