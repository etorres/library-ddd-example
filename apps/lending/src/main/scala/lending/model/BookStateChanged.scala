package es.eriktorr.library
package lending.model

import book.model.{BookId, BookType}
import shared.{DomainEvent, EventId}

import java.time.Instant
import java.util.UUID

sealed abstract class BookStateChanged(
    eventId: EventId,
    when: Instant,
    patronId: PatronId,
    bookId: BookId,
    libraryBranchId: LibraryBranchId,
) extends DomainEvent:
  override val aggregateId: UUID = patronId.value

object BookStateChanged:
  final case class BookCheckedOut(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
      till: Instant,
  ) extends BookStateChanged(eventId, when, patronId, bookId, libraryBranchId)

  final case class BookHoldCanceled(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      libraryBranchId: LibraryBranchId,
  ) extends BookStateChanged(eventId, when, patronId, bookId, libraryBranchId)

  final case class BookHoldExpired(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      libraryBranchId: LibraryBranchId,
  ) extends BookStateChanged(eventId, when, patronId, bookId, libraryBranchId)

  final case class BookPlacedOnHold(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
      holdFrom: Instant,
      holdTill: Option[Instant],
  ) extends BookStateChanged(eventId, when, patronId, bookId, libraryBranchId)

  final case class BookReturned(
      eventId: EventId,
      when: Instant,
      patronId: PatronId,
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
  ) extends BookStateChanged(eventId, when, patronId, bookId, libraryBranchId)
