package es.eriktorr.library
package lending.model

import book.model.{BookId, BookInstance, BookInstanceAddedToCatalogue, BookType}
import lending.model.BookStateChange.*

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

  object AvailableBook:
    def from(
        bookInstanceAddedToCatalogue: BookInstanceAddedToCatalogue,
        libraryBranchId: LibraryBranchId,
    ): AvailableBook =
      val bookInstance = bookInstanceAddedToCatalogue.bookInstance
      AvailableBook(bookInstance.bookId, bookInstance.bookType, libraryBranchId)

    def from(bookOnHold: BookOnHold, bookHoldExpired: BookHoldExpired): AvailableBook =
      AvailableBook(
        bookHoldExpired.bookId,
        bookOnHold.bookType,
        bookHoldExpired.libraryBranchId,
      )

    def from(bookOnHold: BookOnHold, bookHoldCanceled: BookHoldCanceled): AvailableBook =
      AvailableBook(bookHoldCanceled.bookId, bookOnHold.bookType, bookHoldCanceled.libraryBranchId)

    def from(bookOnHold: BookOnHold, bookReturned: BookReturned): AvailableBook =
      AvailableBook(bookReturned.bookId, bookOnHold.bookType, bookReturned.libraryBranchId)

  final case class BookOnHold(
      bookId: BookId,
      bookType: BookType,
      holdPlacedAt: LibraryBranchId,
      byPatron: PatronId,
      holdTill: Option[Instant],
  ) extends Book

  object BookOnHold:
    def from(bookPlacedOnHold: BookPlacedOnHold): BookOnHold = BookOnHold(
      bookPlacedOnHold.bookId,
      bookPlacedOnHold.bookType,
      bookPlacedOnHold.libraryBranchId,
      bookPlacedOnHold.patronId,
      bookPlacedOnHold.holdTill,
    )

  final case class CheckedOutBook(
      bookId: BookId,
      bookType: BookType,
      checkedOutAt: LibraryBranchId,
      byPatron: PatronId,
  ) extends Book

  object CheckedOutBook:
    def from(bookCheckedOut: BookCheckedOut): CheckedOutBook = CheckedOutBook(
      bookCheckedOut.bookId,
      bookCheckedOut.bookType,
      bookCheckedOut.libraryBranchId,
      bookCheckedOut.patronId,
    )
