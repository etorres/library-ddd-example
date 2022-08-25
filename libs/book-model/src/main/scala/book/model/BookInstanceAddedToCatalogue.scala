package es.eriktorr.library
package book.model

import shared.DomainEvent
import shared.refined.types.UUID

import java.time.Instant

final case class BookInstanceAddedToCatalogue(
    eventId: UUID,
    when: Instant,
    bookInstance: BookInstance,
) extends DomainEvent(eventId, when):
  override val aggregateId: UUID = bookInstance.bookId
