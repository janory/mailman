package com.janory.mailman.service.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{extractLog, onSuccess, pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.janory.mailman.service.MailmanRouter.CreateMailbox

import scala.concurrent.duration._

object MailmanRoutes {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val timeout: Timeout = Timeout(5.seconds)

  def apply(mailmanRouter: ActorRef): Route =
    pathPrefix("mailboxes") {
      pathEnd {
        post {
          extractLog { log =>
            onSuccess(mailmanRouter ? CreateMailbox) {
              case mailboxName: String =>
                log.info("Created mailbox: {}", mailboxName)
                complete((StatusCodes.Created, mailboxName))
            }
          }
        }
      } ~
      pathPrefix(Segment) { mailboxName =>
        pathEnd {
          delete {
            complete()
          }
        } ~
        pathPrefix("messages") {
          pathEnd {
            post {
              complete()
            } ~
            get {
              complete(mailboxName)
            } ~
            delete {
              complete()
            }
          } ~
          path(IntNumber) { mailId =>
            get {
              complete(s"$mailboxName $mailId")
            } ~
            delete {
              complete()
            }
          }
        }
      }
    }
}
