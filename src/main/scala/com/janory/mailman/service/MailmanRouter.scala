package com.janory.mailman.service

import java.time.Instant
import java.util.UUID

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import com.janory.mailman.service.MailboxStorage.Mail
import com.janory.mailman.service.MailmanRouter._

import scala.collection.SortedMap

object MailmanRouter {

  final val Name = "mailman-router"

  case object CreateMailbox
  case object MailboxNotFound
  case object MailNotFound
  case class Mailbox(name: String)
  case class AddMessage(mailboxName: String, mail: Mail)
  case class GetMessagesByMailbox()
  case class GetMessageById(mailboxName: String, mailId: Int)
  case class DeleteMailbox(mailboxName: String)
  case class DeleteMessageById()

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

  def receive = receive(Map.empty)

  def receive(mailboxes: Map[String, ActorRef]): Receive = {

    case CreateMailbox =>
      val newName = startMailbox()
      sender() ! Mailbox(newName._1)
      context.become(receive(mailboxes + newName))

    case msg @ AddMessage(mailboxName, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) => {
          mailbox ! (sender(), msg)
        }
        case None => {
          sender() ! MailboxNotFound
        }
      }

    case GetMessagesByMailbox => ()

    case msg @ GetMessageById(mailboxName, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) => {
          mailbox ! (sender(), msg)
        }
        case None => {
          sender() ! MailboxNotFound
        }
      }

    case DeleteMailbox(mailboxName) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) => {
          mailbox ! PoisonPill
          sender() ! s"Mailbox '$mailboxName' is deleted!"
        }
        case None => {
          sender() ! MailboxNotFound
        }
      }

    case DeleteMessageById => ()
  }

  def startMailbox(): (String, ActorRef) = {
    val mailboxName = s"${UUID.randomUUID().toString}@mailman.com"
    mailboxName -> context.actorOf(MailboxStorage(), s"mailbox-$mailboxName")
  }
}

object MailboxStorage {
  def apply() = Props(new MailboxStorage())

  case class Mail(id: Option[Int],
                  datetime: Option[Instant],
                  from: String,
                  to: Vector[String],
                  cc: Vector[String] = Vector.empty,
                  subject: String,
                  content: String) {}
}

class MailboxStorage extends Actor {
  import MailboxStorage._

  def receive = receive(SortedMap.empty[Int, Mail])

  def receive(mails: SortedMap[Int, Mail]): Receive = {

    case (receiver: ActorRef, AddMessage(_, mail)) =>
      val newId         = if (mails.isEmpty) 1 else mails.lastKey + 1
      val mailToPersist = mail.copy(id = Some(newId), datetime = Some(Instant.now()))
      receiver ! mailToPersist
      context.become(
        receive(
          mails + (newId -> mailToPersist)
        )
      )

    case message: GetMessagesByMailbox => ()

    case (receiver: ActorRef, message: GetMessageById) =>
      mails.get(message.mailId) match {
        case Some(mail) => {
          receiver ! mail
        }
        case None => {
          receiver ! MailNotFound
        }
      }

    case message: DeleteMessageById => ()
  }
}
