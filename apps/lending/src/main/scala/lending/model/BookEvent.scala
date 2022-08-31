package es.eriktorr.library
package lending.model

import book.model.{BookId, BookType}
import shared.{DomainEvent, EventId}

import java.time.Instant
import java.util.UUID

sealed abstract class BookEvent(eventId: EventId, when: Instant) extends DomainEvent(eventId, when)

object BookEvent:
  final case class BookCheckedOut(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
      till: Instant,
  ) extends BookEvent(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookHoldCanceled(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      libraryBranchId: LibraryBranchId,
  ) extends BookEvent(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookHoldExpired(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      libraryBranchId: LibraryBranchId,
  ) extends BookEvent(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookPlacedOnHold(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
      holdFrom: Instant,
      holdTill: Option[Instant],
  ) extends BookEvent(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookReturned(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
  ) extends BookEvent(eventId, when):
    override val aggregateId: UUID = patronId.value
