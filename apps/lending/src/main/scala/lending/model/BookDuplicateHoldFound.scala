package es.eriktorr.library
package lending.model

import book.model.BookId
import lending.model.Book.BookOnHold
import lending.model.BookStateChanged.BookPlacedOnHold
import shared.{DomainEvent, EventId}

import java.time.Instant
import java.util.UUID

final case class BookDuplicateHoldFound(
    eventId: EventId,
    when: Instant,
    firstPatronId: PatronId,
    secondPatronId: PatronId,
    libraryBranchId: LibraryBranchId,
    bookId: BookId,
) extends DomainEvent:
  override val aggregateId: UUID = bookId.value

object BookDuplicateHoldFound:
  def from(
      eventId: EventId,
      when: Instant,
      bookOnHold: BookOnHold,
      bookPlacedOnHold: BookPlacedOnHold,
  ): BookDuplicateHoldFound = BookDuplicateHoldFound(
    eventId,
    when,
    firstPatronId = bookOnHold.byPatron,
    secondPatronId = bookPlacedOnHold.patronId,
    libraryBranchId = bookPlacedOnHold.libraryBranchId,
    bookId = bookPlacedOnHold.bookId,
  )
