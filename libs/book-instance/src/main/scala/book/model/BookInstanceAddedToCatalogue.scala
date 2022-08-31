package es.eriktorr.library
package book.model

import shared.{DomainEvent, EventId}

import java.time.Instant
import java.util.UUID

final case class BookInstanceAddedToCatalogue(
    eventId: EventId,
    when: Instant,
    bookInstance: BookInstance,
) extends DomainEvent(eventId, when):
  override val aggregateId: UUID = bookInstance.bookId.value
