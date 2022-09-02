package es.eriktorr.library
package shared

import java.time.Instant
import java.util.UUID

trait DomainEvent:
  val eventId: EventId
  val when: Instant
  val aggregateId: UUID
