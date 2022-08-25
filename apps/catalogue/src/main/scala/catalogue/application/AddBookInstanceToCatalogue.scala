package es.eriktorr.library
package catalogue.application

import book.model.{BookInstance, BookInstanceAddedToCatalogue}
import catalogue.model.{BookInstanceAddedToCatalogueEventPublisher, Catalogue}
import shared.refined.types.UUID

import cats.effect.std.UUIDGen
import cats.effect.{Clock, IO}

final class AddBookInstanceToCatalogue(
    catalogue: Catalogue,
    eventPublisher: BookInstanceAddedToCatalogueEventPublisher,
)(implicit clock: Clock[IO]):
  def add(bookInstance: BookInstance): IO[Unit] = for
    eventId <- UUIDGen.randomUUID[IO].map(UUID.fromJava)
    when <- clock.realTimeInstant
    _ <- catalogue.add(bookInstance)
    _ <- eventPublisher.publish(BookInstanceAddedToCatalogue(eventId, when, bookInstance))
  yield ()
