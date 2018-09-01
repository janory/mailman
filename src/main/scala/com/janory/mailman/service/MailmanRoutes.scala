package com.janory.mailman.service

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import scala.concurrent.duration._

object MailmanRoutes {

  implicit val timeout: Timeout = Timeout(5.seconds)

  def apply(): Route = path("hello") {
    get {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
    }
  }

}
