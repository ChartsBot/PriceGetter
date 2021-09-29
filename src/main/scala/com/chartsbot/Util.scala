package com.chartsbot

import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.{ DefaultFormats, Formats }

object Util {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  def createServerError(errorType: String, message: String): String = {
    val errorMessage = "error" ->
      ("error type" -> errorType) ~
      ("message" -> message.replaceAll(System.lineSeparator, ""))
    compact(render(errorMessage))
  }

}
