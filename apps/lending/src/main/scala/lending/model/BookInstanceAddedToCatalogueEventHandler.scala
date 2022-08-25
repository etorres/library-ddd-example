package es.eriktorr.library
package lending.model

import book.model.BookInstanceAddedToCatalogue

import cats.effect.IO
import fs2.Stream

trait BookInstanceAddedToCatalogueEventHandler:
  def handleWith(f: BookInstanceAddedToCatalogue => IO[Unit]): Stream[IO, Unit]
