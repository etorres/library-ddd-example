package es.eriktorr.library
package lending.model

import book.model.{BookId, BookType}
import shared.{DomainEvent, EventId}

import java.time.Instant
import java.util.UUID

sealed abstract class BookStateChange(eventId: EventId, when: Instant)
    extends DomainEvent(eventId, when)

object BookStateChange:
  final case class BookCheckedOut(
      override val eventId: EventId,
      override val when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
      till: Instant,
  ) extends BookStateChange(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookHoldCanceled(
      override val eventId: EventId,
      override val when: Instant,
      patronId: PatronId,
      bookId: BookId,
      libraryBranchId: LibraryBranchId,
  ) extends BookStateChange(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookHoldExpired(
      override val eventId: EventId,
      override val when: Instant,
      patronId: PatronId,
      bookId: BookId,
      libraryBranchId: LibraryBranchId,
  ) extends BookStateChange(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookPlacedOnHold(
      override val eventId: EventId,
      override val when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
      holdFrom: Instant,
      holdTill: Option[Instant],
  ) extends BookStateChange(eventId, when):
    override val aggregateId: UUID = patronId.value

  final case class BookReturned(
      override val eventId: EventId,
      override val when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
  ) extends BookStateChange(eventId, when):
    override val aggregateId: UUID = patronId.value
