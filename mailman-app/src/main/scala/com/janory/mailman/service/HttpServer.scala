package com.janory.mailman.service

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props, SupervisorStrategy}
import akka.event.LoggingReceive
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer

object HttpServer {

  final val Name = "http-server"

  def apply(routes: Route, interface: String, port: Int): Props =
    Props(new HttpServer(routes, interface, port))

}

class HttpServer(routes: Route, interface: String, port: Int) extends Actor with ActorLogging {

  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import context.dispatcher

  Http(context.system).bindAndHandle(routes, interface, port).pipeTo(self)

  var serverBinding: ServerBinding = _

  override def receive: Receive = initialize

  def initialize: Receive = LoggingReceive {
    case binding: ServerBinding =>
      log.info("{} bound to {}", self, binding.localAddress)
      serverBinding = binding
      context.become(running)
    case Failure(cause) =>
      log.error(cause, "Server binding failed, stop {}", self)
      context.stop(self)
  }

  def running: Receive = Actor.emptyBehavior

  override def postStop(): Unit =
    serverBinding.unbind()
}
