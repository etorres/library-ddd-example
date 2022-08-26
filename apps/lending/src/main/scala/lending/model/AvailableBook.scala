package es.eriktorr.library
package lending.model

import book.model.{BookInstance, BookType}
import lending.model.AvailableBook.Book
import shared.refined.types.UUID

final case class AvailableBook(book: Book, libraryBranchId: UUID):
  def bookId: UUID = book.bookId
  def bookType: BookType = book.bookType

object AvailableBook:
  final case class Book(bookId: UUID, bookType: BookType)

  object Book:
    def from(bookInstance: BookInstance): Book =
      Book(bookInstance.bookId, bookInstance.bookType)

  def from(bookInstance: BookInstance, libraryBranchId: UUID): AvailableBook =
    AvailableBook(Book.from(bookInstance), libraryBranchId)
