package es.eriktorr.library
package book.model

import shared.{DomainEvent, EventId}

import java.time.Instant
import java.util.UUID

final case class BookInstanceAddedToCatalogue(
    override val eventId: EventId,
    override val when: Instant,
    bookInstance: BookInstance,
) extends DomainEvent(eventId, when):
  override val aggregateId: UUID = bookInstance.bookId.value
