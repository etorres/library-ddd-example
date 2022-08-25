package es.eriktorr.library
package shared

import shared.refined.types.UUID

import java.time.Instant

abstract class DomainEvent(eventId: UUID, when: Instant):
  val aggregateId: UUID
