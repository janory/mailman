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
  case object MailboxDeleted
  case object MailRemoved
  case class PagedMails(page: Int,
                        nextPage: Option[Int],
                        numberOfPages: Int,
                        numberOfEntries: Int,
                        mails: Iterable[Mail])
  case class Mailbox(name: String)
  case class AddMail(mailboxName: String, mail: Mail)
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

  def receive = receive(Map.empty)

  def receive(mailboxes: Map[String, ActorRef]): Receive = {

    case CreateMailbox =>
      val newName = startMailbox()
      context.become(receive(mailboxes + newName))
      sender() ! Mailbox(newName._1)

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
  import scala.math.ceil

  def receive = receive(SortedMap.empty[Int, Mail])

  def receive(mails: SortedMap[Int, Mail]): Receive = {

    case (receiver: ActorRef, AddMail(_, mail)) =>
      val newId         = if (mails.isEmpty) 1 else mails.lastKey + 1
      val mailToPersist = mail.copy(id = Some(newId), datetime = Some(Instant.now()))
      context.become(
        receive(
          mails + (newId -> mailToPersist)
        )
      )
      receiver ! mailToPersist

    case (receiver: ActorRef, GetMailByMailbox(_, page, size)) =>
      val mailboxSize   = mails.size
      val startItem     = (page - 1) * size
      val numberOfPages = ceil(mailboxSize.toDouble / size.toDouble).toInt
      val nextPage      = if (page < numberOfPages) Some(page + 1) else None
      receiver ! PagedMails(page,
                            nextPage,
                            numberOfPages,
                            mailboxSize,
                            mails.slice(startItem, startItem + size).values)

    case (receiver: ActorRef, message: GetMailById) =>
      mails.get(message.mailId) match {
        case Some(mail) =>
          receiver ! mail
        case None =>
          receiver ! MailNotFound
      }

    case (receiver: ActorRef, DeleteMailById(_, mailId)) =>
      mails.get(mailId) match {
        case Some(_) =>
          context.become(
            receive(
              mails - mailId
            )
          )
          receiver ! MailRemoved
        case None =>
          receiver ! MailNotFound
      }
  }
}
