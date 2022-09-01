package es.eriktorr.library
package lending.model

import book.model.BookId

import cats.effect.IO

trait Books:
  def findBy(bookId: BookId): IO[Option[Book]]

  def save(book: Book): IO[Unit]
