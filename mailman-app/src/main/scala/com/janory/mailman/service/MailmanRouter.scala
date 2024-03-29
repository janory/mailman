package com.janory.mailman.service

import java.util.UUID

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import com.janory.mailman.service.MailboxStorage.NewMail

import scala.collection.immutable.HashMap

object MailmanRouter {

  final val Name = "mailman-router"

  case object CreateMailbox
  case object MailboxNotFound
  case object MailNotFound
  case object MailboxDeleted
  case object MailRemoved
  case class Mailbox(name: String)
  case class AddMail(mailboxName: String, mail: NewMail)
  case class GetMailByMailbox(mailboxName: String, page: Int, size: Int)
  case class GetMailById(mailboxName: String, mailId: Int)
  case class DeleteMailbox(mailboxName: String)
  case class DeleteMailById(mailboxName: String, mailId: Int)

  def apply() =
    Props(
      new MailmanRouter()
    )

}

class MailmanRouter() extends Actor {

  import MailmanRouter._

  override def preStart(): Unit =
    super.preStart()

  override def postStop(): Unit =
    super.postStop()

  def receive = receive(HashMap.empty)

  def receive(mailboxes: HashMap[String, ActorRef]): Receive = {

    case CreateMailbox =>
      val newMailbox = startMailbox()
      context.become(receive(mailboxes + newMailbox))
      sender() ! Mailbox(newMailbox._1)

    case msg @ AddMail(mailboxName, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) =>
          mailbox ! (sender(), msg)
        case None =>
          sender() ! MailboxNotFound
      }

    case msg @ GetMailByMailbox(mailboxName, _, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) =>
          mailbox ! (sender(), msg)
        case None =>
          sender() ! MailboxNotFound
      }

    case msg @ GetMailById(mailboxName, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) =>
          mailbox ! (sender(), msg)
        case None =>
          sender() ! MailboxNotFound
      }

    case DeleteMailbox(mailboxName) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) =>
          mailbox ! PoisonPill
          context.become(receive(mailboxes - mailboxName))
          sender() ! MailboxDeleted
        case None =>
          sender() ! MailboxNotFound
      }

    case msg @ DeleteMailById(mailboxName, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) =>
          mailbox ! (sender(), msg)
        case None =>
          sender() ! MailboxNotFound
      }
  }

  def startMailbox(): (String, ActorRef) = {
    val mailboxName = s"${UUID.randomUUID().toString}@mailman.com"
    mailboxName -> context.actorOf(MailboxStorage(), s"mailbox-$mailboxName")
  }
}
