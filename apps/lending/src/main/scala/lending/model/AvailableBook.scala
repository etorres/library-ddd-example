package es.eriktorr.library
package lending.model

import book.model.{BookId, BookInstance, BookType}

final case class AvailableBook(bookId: BookId, bookType: BookType, libraryBranchId: LibraryBranchId)

object AvailableBook:
  def from(bookInstance: BookInstance, libraryBranchId: LibraryBranchId): AvailableBook =
    AvailableBook(bookInstance.bookId, bookInstance.bookType, libraryBranchId)
