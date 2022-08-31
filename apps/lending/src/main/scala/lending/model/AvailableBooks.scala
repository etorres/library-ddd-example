package es.eriktorr.library
package lending.model

import book.model.BookId

import cats.effect.IO

trait AvailableBooks:
  def add(availableBook: AvailableBook): IO[Unit]

  def findBy(bookId: BookId): IO[Option[AvailableBook]]
