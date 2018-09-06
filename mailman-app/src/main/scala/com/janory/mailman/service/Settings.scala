package com.janory.mailman.service

import akka.http.scaladsl.model.Uri
import com.janory.mailman.service.Settings.Http

import scala.concurrent.duration.FiniteDuration

object Settings {

  case class Http(interface: String, port: Int)

}

case class Settings(http: Http)
