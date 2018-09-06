package com.janory.mailman.service

import java.time.Instant

import akka.actor.{Actor, ActorRef, Props}
import com.janory.mailman.service.MailmanRouter._

import scala.collection.immutable.SortedMap

object MailboxStorage {
  def apply() = Props(new MailboxStorage())

  case class PagedMails(page: Int,
                        nextPage: Option[Int],
                        numberOfPages: Int,
                        numberOfEntries: Int,
                        mails: Vector[StrippedMail])

  case class StrippedMail(id: Int, from: String, subject: String)

  case class NewMail(from: String,
                     to: Vector[String],
                     cc: Vector[String] = Vector.empty,
                     subject: String,
                     content: String) {}

  case class Mail(id: Int,
                  datetime: Instant,
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
      val newId = if (mails.isEmpty) 1 else mails.lastKey + 1
      val mailToPersist = Mail(id = newId,
                               datetime = Instant.now(),
                               from = mail.from,
                               to = mail.to,
                               cc = mail.cc,
                               subject = mail.subject,
                               content = mail.content)
      context.become(
        receive(
          mails + (newId -> mailToPersist)
        )
      )
      receiver ! mailToPersist

    case (receiver: ActorRef, GetMailByMailbox(_, page, size)) =>
      val mailboxSize   = mails.size
      val startItem     = mailboxSize - page * size
      val numberOfPages = ceil(mailboxSize.toDouble / size.toDouble).toInt
      val nextPage      = if (page < numberOfPages) Some(page + 1) else None
      receiver ! PagedMails(
        page,
        nextPage,
        numberOfPages,
        mailboxSize,
        mails
          .slice(startItem, startItem + size)
          .values
          .toVector
          .map(mail => StrippedMail(mail.id, mail.from, mail.subject))
          .reverse
      )

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
