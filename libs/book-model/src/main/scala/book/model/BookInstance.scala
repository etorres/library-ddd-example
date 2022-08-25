package es.eriktorr.library
package book.model

import shared.refined.types.UUID

final case class BookInstance(bookId: UUID, isbn: ISBN, bookType: BookType)
