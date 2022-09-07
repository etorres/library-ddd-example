package es.eriktorr.library
package lending.model

import book.model.BookId
import lending.model.Book.AvailableBook

import cats.effect.IO

trait AvailableBooks:
  def findAvailableBookBy(bookId: BookId): IO[Option[AvailableBook]]
