package es.eriktorr.library
package lending.model

import book.model.{Book, BookInstance}
import shared.refined.types.UUID

import cats.effect.IO

trait AvailableBooks:
  def add(availableBook: AvailableBook): IO[Unit]

  def findBy(bookId: UUID): IO[Option[AvailableBook]]
