package es.eriktorr.library
package catalogue.application

import book.model.{BookInstance, BookInstanceAddedToCatalogue}
import catalogue.model.{BookInstanceAddedToCatalogueEventPublisher, Catalogue}
import shared.infrastructure.EventPublisher
import shared.refined.types.infrastructure.UUIDGenerator

import cats.effect.{Clock, IO}

final class AddBookInstanceToCatalogue(
    catalogue: Catalogue,
    eventPublisher: EventPublisher[BookInstanceAddedToCatalogue],
)(using clock: Clock[IO], uuidGenerator: UUIDGenerator[IO]):
  def add(bookInstance: BookInstance): IO[Unit] = for
    when <- clock.realTimeInstant
    eventId <- uuidGenerator.randomUUID
    _ <- catalogue.add(bookInstance)
    _ <- eventPublisher.publish(BookInstanceAddedToCatalogue(eventId, when, bookInstance))
  yield ()
