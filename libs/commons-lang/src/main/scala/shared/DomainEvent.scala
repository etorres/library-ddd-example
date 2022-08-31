package es.eriktorr.library
package shared

import java.time.Instant
import java.util.UUID

abstract class DomainEvent(eventId: EventId, when: Instant):
  val aggregateId: UUID
