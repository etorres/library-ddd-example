package es.eriktorr.library

import refined.types.UUID

import java.time.Instant

abstract class DomainEvent(eventId: UUID, when: Instant):
  val aggregateId: UUID
