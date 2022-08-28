package es.eriktorr.library
package catalogue.model

import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.EventPublisher

import cats.effect.IO

trait BookInstanceAddedToCatalogueEventPublisher
    extends EventPublisher[BookInstanceAddedToCatalogue]:
  override def publish(event: BookInstanceAddedToCatalogue): IO[Unit]
