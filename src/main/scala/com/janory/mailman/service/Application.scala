package com.janory.mailman.service

import akka.actor.{Actor, ActorLogging, Props, SupervisorStrategy, Terminated}
import com.janory.mailman.service.routes.MailmanRoutes

object Application {

  final val Name = "application"

  def apply(settings: Settings): Props =
    Props(new Application(settings))
}

class Application(settings: Settings) extends Actor with ActorLogging {

  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  private val mailmanRouter = context.actorOf(MailmanRouter(), MailmanRouter.Name)


  private val httpServer = context.actorOf(
    HttpServer(
      MailmanRoutes(mailmanRouter),
      settings.http.interface,
      settings.http.port
    )
  )
  context.watch(httpServer)

  override def receive: Receive = {
    case Terminated(dead) =>
      log.error("{} is dead, stop {}", dead, self)
      context.stop(self)
  }
}
