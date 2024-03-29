package com.janory.mailman.service.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{extractLog, onSuccess, pathEnd, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.janory.mailman.service.MailboxStorage.{Mail, NewMail, PagedMails}
import com.janory.mailman.service.MailmanRouter.{MailboxNotFound, _}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.circe.parser._
import io.circe.java8.time._

import scala.concurrent.duration._

object MailmanRoutes extends FailFastCirceSupport {

  implicit val timeout: Timeout            = Timeout(2.seconds)
  implicit val customConfig: Configuration = Configuration.default.withDefaults

  private val MailboxNotFoundMessage = parse("""{ "message": "Mailbox not found!" }""").right.get
  private val MailNotFoundMessage    = parse("""{ "message": "Mail not found!" }""").right.get

  final val DefaultPageSize = 10

  def apply(mailmanRouter: ActorRef): Route =
    pathPrefix("mailboxes") {
      extractLog { log =>
        pathEnd {
          post {
            onSuccess(mailmanRouter ? CreateMailbox) {
              case mailbox: Mailbox =>
                log.info("[CREATED] Mailbox: {}", mailbox.name)
                complete((StatusCodes.Created, mailbox))
              case MailboxNotFound => complete(StatusCodes.NotFound, MailboxNotFoundMessage)
            }
          }
        } ~
        pathPrefix(Segment) { mailboxName =>
          pathEnd {
            delete {
              onSuccess(mailmanRouter ? DeleteMailbox(mailboxName)) {
                case MailboxDeleted =>
                  log.info("[DELETED] Mailbox: {}", mailboxName)
                  complete(StatusCodes.NoContent)
                case MailboxNotFound => complete(StatusCodes.NotFound, MailboxNotFoundMessage)
              }
            }
          } ~
          pathPrefix("messages") {
            pathEnd {
              post {
                entity(as[NewMail]) { newMail =>
                  onSuccess(mailmanRouter ? AddMail(mailboxName, newMail)) {
                    case persistedMail: Mail =>
                      log.info("[CREATED] Mail by id: {} for Mailbox: {}",
                               persistedMail.id,
                               mailboxName)
                      complete((StatusCodes.Created, persistedMail))
                    case MailboxNotFound => complete(StatusCodes.NotFound, MailboxNotFoundMessage)
                  }
                }
              } ~
              get {
                parameter('page ? 1, 'size ? DefaultPageSize) { (page, size) =>
                  onSuccess(mailmanRouter ? GetMailByMailbox(mailboxName, page, size)) {
                    case mails: PagedMails => complete((StatusCodes.OK, mails))
                    case MailboxNotFound   => complete(StatusCodes.NotFound, MailboxNotFoundMessage)
                  }
                }
              }
            } ~
            path(IntNumber) { mailId =>
              get {
                onSuccess(mailmanRouter ? GetMailById(mailboxName, mailId)) {
                  case mail: Mail      => complete((StatusCodes.OK, mail))
                  case MailboxNotFound => complete(StatusCodes.NotFound, MailboxNotFoundMessage)
                  case MailNotFound    => complete(StatusCodes.NotFound, MailNotFoundMessage)
                }
              } ~
              delete {
                onSuccess(mailmanRouter ? DeleteMailById(mailboxName, mailId)) {
                  case MailRemoved =>
                    log.info("[DELETED] Mail by id: {} for Mailbox: {}", mailId, mailboxName)
                    complete(StatusCodes.NoContent)
                  case MailboxNotFound => complete(StatusCodes.NotFound, MailboxNotFoundMessage)
                  case MailNotFound    => complete(StatusCodes.NotFound, MailNotFoundMessage)
                }
              }
            }
          }
        }
      }
    }
}
