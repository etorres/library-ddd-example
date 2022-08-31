package es.eriktorr.library
package lending.model

import book.model.{BookId, BookInstance, BookType}
import shared.ValidationError
import shared.validated.AllErrorsOr

import cats.syntax.all.*

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
        bookId: BookId,
        bookType: BookType,
        maybeLibraryBranchId: Option[LibraryBranchId],
    ): AllErrorsOr[AvailableBook] =
      (
        bookId.validNec,
        bookType.validNec,
        maybeLibraryBranchId.fold(
          InvalidBook(s"Missing library branch Id for available book: $bookId").invalidNec,
        )(_.validNec),
      ).mapN(AvailableBook.apply)

    def from(bookInstance: BookInstance, libraryBranchId: LibraryBranchId): AvailableBook =
      AvailableBook(bookInstance.bookId, bookInstance.bookType, libraryBranchId)

  final case class BookOnHold(
      bookId: BookId,
      bookType: BookType,
      holdPlacedAt: LibraryBranchId,
      byPatron: PatronId,
      holdTill: Option[Instant],
  ) extends Book

  object BookOnHold:
    def from(
        bookId: BookId,
        bookType: BookType,
        maybeHoldPlacedAt: Option[LibraryBranchId],
        maybeByPatron: Option[PatronId],
        holdTill: Option[Instant],
    ): AllErrorsOr[BookOnHold] = (
      bookId.validNec,
      bookType.validNec,
      maybeHoldPlacedAt.fold(
        InvalidBook(s"Missing library branch for book on hold: $bookId").invalidNec,
      )(_.validNec),
      maybeByPatron.fold(InvalidBook(s"Missing patron for book on hold: $bookId").invalidNec)(
        _.validNec,
      ),
      holdTill.validNec,
    ).mapN(BookOnHold.apply)

  final case class CheckedOutBook(
      bookId: BookId,
      bookType: BookType,
      checkedOutAt: LibraryBranchId,
      byPatron: PatronId,
  ) extends Book

  object CheckedOutBook:
    def from(
        bookId: BookId,
        bookType: BookType,
        maybeCheckedOutAt: Option[LibraryBranchId],
        maybeByPatron: Option[PatronId],
    ): AllErrorsOr[CheckedOutBook] =
      (
        bookId.validNec,
        bookType.validNec,
        maybeCheckedOutAt.fold(
          InvalidBook(s"Missing library branch for checked out book: $bookId").invalidNec,
        )(_.validNec),
        maybeByPatron.fold(InvalidBook(s"Missing patron for checked out book: $bookId").invalidNec)(
          _.validNec,
        ),
      ).mapN(CheckedOutBook.apply)

  final case class InvalidBook(message: String) extends ValidationError(message)
