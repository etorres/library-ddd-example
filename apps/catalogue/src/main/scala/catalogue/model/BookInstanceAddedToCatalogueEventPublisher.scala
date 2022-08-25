package es.eriktorr.library
package catalogue.model

import book.model.BookInstanceAddedToCatalogue

import cats.effect.IO

trait BookInstanceAddedToCatalogueEventPublisher:
  def publish(event: BookInstanceAddedToCatalogue): IO[Unit]
