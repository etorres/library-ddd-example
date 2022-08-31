package es.eriktorr.library
package catalogue.application

import book.model.{BookInstance, BookInstanceAddedToCatalogue}
import catalogue.model.Catalogue
import shared.EventId
import shared.infrastructure.EventPublisher

import cats.effect.std.UUIDGen
import cats.effect.{Clock, IO}

final class AddBookInstanceToCatalogue(
    catalogue: Catalogue,
    eventPublisher: EventPublisher[BookInstanceAddedToCatalogue],
)(using clock: Clock[IO], uuidGenerator: UUIDGen[IO]):
  def add(bookInstance: BookInstance): IO[Unit] = for
    _ <- catalogue.add(bookInstance)
    _ <- publishEvent(bookInstance)
  yield ()

  private[this] def publishEvent(bookInstance: BookInstance) = for
    eventId <- uuidGenerator.randomUUID.map(EventId.from)
    when <- clock.realTimeInstant
    _ <- eventPublisher.publish(BookInstanceAddedToCatalogue(eventId, when, bookInstance))
  yield ()
