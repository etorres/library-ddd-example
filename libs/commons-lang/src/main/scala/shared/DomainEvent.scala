package es.eriktorr.library
package shared

import java.time.Instant
import java.util.UUID

abstract class DomainEvent(val eventId: EventId, val when: Instant):
  val aggregateId: UUID
