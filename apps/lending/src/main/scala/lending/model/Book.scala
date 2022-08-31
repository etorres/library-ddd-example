package es.eriktorr.library
package lending.model

import book.model.{BookId, BookInstance, BookType}

import java.time.Instant

sealed trait Book:
  val bookId: BookId
  val bookType: BookType

object Book:
  final case class AvailableBook(
      bookId: BookId,
      bookType: BookType,
      libraryBranchId: LibraryBranchId,
  ) extends Book

  final case class BookOnHold(
      bookId: BookId,
      bookType: BookType,
      holdPlacedAt: LibraryBranchId,
      byPatron: PatronId,
      holdTill: Option[Instant],
  ) extends Book

  final case class CheckedOutBook(
      bookId: BookId,
      bookType: BookType,
      checkedOutAt: LibraryBranchId,
      byPatron: PatronId,
  ) extends Book

  def availableBookFrom(bookInstance: BookInstance, libraryBranchId: LibraryBranchId): Book =
    AvailableBook(bookInstance.bookId, bookInstance.bookType, libraryBranchId)
