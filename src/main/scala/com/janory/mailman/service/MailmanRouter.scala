package com.janory.mailman.service

import java.util.UUID

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import com.janory.mailman.service.Mailbox.Mail
import com.janory.mailman.service.MailmanRouter._

import scala.collection.SortedMap

object MailmanRouter {

  final val Name = "mailman-router"

  case object CreateMailbox
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
      sender() ! newName._1
      context.become(receive(mailboxes + newName))

    case msg @ AddMessage(mailboxName, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) => {
          mailbox ! msg
          sender() ! s"Mail '$mailbox' added to the mailbox"
        }
        case None => {
          sender() ! s"Mailbox '$mailboxName' does not exist!"
        }
      }

    case GetMessagesByMailbox => ()

    case msg @ GetMessageById(mailboxName, _) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) => {
          mailbox ! (sender(), msg)
        }
        case None => {
          sender() ! s"Mailbox '$mailboxName' does not exist!"
        }
      }

    case DeleteMailbox(mailboxName) =>
      mailboxes.get(mailboxName) match {
        case Some(mailbox) => {
          mailbox ! PoisonPill
          sender() ! s"Mailbox '$mailboxName' is deleted!"
        }
        case None => {
          sender() ! s"Mailbox '$mailboxName' does not exist!"
        }
      }

    case DeleteMessageById => ()
  }

  def startMailbox(): (String, ActorRef) = {
    val mailboxName = s"${UUID.randomUUID().toString}@mailman.com"
    mailboxName -> context.actorOf(Mailbox(), s"mailbox-$mailboxName")
  }
}

object Mailbox {
  def apply() = Props(new Mailbox())

  case class Mail()
}

class Mailbox extends Actor {
  import Mailbox._

  def receive = receive(SortedMap.empty[Int, Mail])

  def receive(mails: SortedMap[Int, Mail]): Receive = {

    case AddMessage(_, mail) =>
      context.become(receive(mails + (mails.firstKey + 1 -> mail)))

    case message: GetMessagesByMailbox => ()

    case (receiver: ActorRef, message: GetMessageById) =>
      mails.get(message.mailId) match {
        case Some(mail) => {
          receiver ! mail
        }
        case None => {
          receiver ! s"Mail does not exist with id ${message.mailId}!"
        }
      }

    case message: DeleteMessageById    => ()
  }
}
