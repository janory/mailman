package com.janory.mailman.service

import akka.actor.ActorSystem
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("mailman-service")

  val config = system.settings.config

  val settings =
    Settings(config.as[Settings.Http]("http"))

  val service = system.actorOf(Application(settings), Application.Name)
}
