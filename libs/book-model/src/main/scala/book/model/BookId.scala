package es.eriktorr.library
package book.model

import java.util.UUID

opaque type BookId = UUID

object BookId:
  def from(value: UUID): BookId = value

  extension (bookId: BookId) def value: UUID = bookId
