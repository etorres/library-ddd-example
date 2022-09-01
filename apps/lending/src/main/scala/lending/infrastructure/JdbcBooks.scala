package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.{BookIdJdbcMapping, BookTypeJdbcMapping}
import book.model.{BookId, BookType}
import lending.infrastructure.JdbcBooks.{BookJdbcMapping, RawBook}
import lending.infrastructure.LibraryBranchIdJdbcMapping
import lending.model.*
import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import lending.model.BookState.{Available, CheckedOut, OnHold}
import shared.ValidationError
import shared.validated.AllErrorsOr
import shared.validated.ValidatedIO.validatedNecIO

import cats.effect.IO
import cats.syntax.all.*
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.Instant
import java.util.UUID

final class JdbcBooks(transactor: Transactor[IO]) extends Books with BookJdbcMapping:
  override def findBy(bookId: BookId): IO[Option[Book]] = sql"""
      SELECT
        book_id,
        book_type,
        book_state,
        available_at_branch,
        on_hold_at_branch,
        on_hold_by_patron,
        checked_out_at_branch,
        checked_out_by_patron,
        on_hold_till
      FROM books
      WHERE book_id = $bookId"""
    .query[RawBook]
    .option
    .transact(transactor)
    .flatMap {
      case Some(rawBook) =>
        (rawBook.bookState match
          case Available => rawBook.asAvailableBook
          case CheckedOut => rawBook.asCheckedOutBook
          case OnHold => rawBook.asBookOnHold
        ).validated.map(Some.apply)
      case None => IO.pure(None)
    }

  override def save(book: Book): IO[Unit] = for
    rawBook <- IO.pure(RawBook.from(book))
    _ <- sql"""
          INSERT INTO books (
            book_id,
            book_type,
            book_state,
            available_at_branch,
            on_hold_at_branch,
            on_hold_by_patron,
            checked_out_at_branch,
            checked_out_by_patron,
            on_hold_till
          ) VALUES (
            ${rawBook.bookId},
            ${rawBook.bookType},
            ${rawBook.bookState},
            ${rawBook.availableAtBranch},
            ${rawBook.onHoldAtBranch},
            ${rawBook.onHoldByPatron},
            ${rawBook.checkedOutAtBranch},
            ${rawBook.checkedOutByPatron},
            ${rawBook.onHoldTill}
          )
          ON CONFLICT (book_id) DO UPDATE SET 
            book_type = EXCLUDED.book_type,
            book_state = EXCLUDED.book_state,
            available_at_branch = EXCLUDED.available_at_branch,
            on_hold_at_branch = EXCLUDED.on_hold_at_branch,
            on_hold_by_patron = EXCLUDED.on_hold_by_patron,
            checked_out_at_branch = EXCLUDED.checked_out_at_branch,
            checked_out_by_patron = EXCLUDED.checked_out_by_patron,
            on_hold_till = EXCLUDED.on_hold_till""".update.run.transact(transactor)
  yield ()

object JdbcBooks:
  trait BookJdbcMapping
      extends BookIdJdbcMapping
      with BookStateJdbcMapping
      with BookTypeJdbcMapping
      with LibraryBranchIdJdbcMapping
      with PatronIdJdbcMapping

  final case class InvalidDatabaseEntity(message: String) extends ValidationError(message)

  final case class RawBook(
      bookId: BookId,
      bookType: BookType,
      bookState: BookState,
      availableAtBranch: Option[LibraryBranchId],
      onHoldAtBranch: Option[LibraryBranchId],
      onHoldByPatron: Option[PatronId],
      checkedOutAtBranch: Option[LibraryBranchId],
      checkedOutByPatron: Option[PatronId],
      onHoldTill: Option[Instant],
  ):
    def asAvailableBook: AllErrorsOr[AvailableBook] =
      (
        bookId.validNec,
        bookType.validNec,
        availableAtBranch.fold(
          InvalidDatabaseEntity(s"Missing library branch for available book: $bookId").invalidNec,
        )(_.validNec),
      ).mapN(AvailableBook.apply)

    def asBookOnHold: AllErrorsOr[BookOnHold] = (
      bookId.validNec,
      bookType.validNec,
      onHoldAtBranch.fold(
        InvalidDatabaseEntity(s"Missing library branch for book on hold: $bookId").invalidNec,
      )(_.validNec),
      onHoldByPatron.fold(
        InvalidDatabaseEntity(s"Missing patron for book on hold: $bookId").invalidNec,
      )(
        _.validNec,
      ),
      onHoldTill.validNec,
    ).mapN(BookOnHold.apply)

    def asCheckedOutBook: AllErrorsOr[CheckedOutBook] = (
      bookId.validNec,
      bookType.validNec,
      checkedOutAtBranch.fold(
        InvalidDatabaseEntity(s"Missing library branch for checked out book: $bookId").invalidNec,
      )(_.validNec),
      checkedOutByPatron.fold(
        InvalidDatabaseEntity(s"Missing patron for checked out book: $bookId").invalidNec,
      )(
        _.validNec,
      ),
    ).mapN(CheckedOutBook.apply)

  object RawBook:
    def from(book: Book): RawBook = book match
      case availableBook: AvailableBook =>
        RawBook(
          bookId = availableBook.bookId,
          bookType = availableBook.bookType,
          bookState = BookState.Available,
          availableAtBranch = Some(availableBook.libraryBranchId),
          onHoldAtBranch = None,
          onHoldByPatron = None,
          checkedOutAtBranch = None,
          checkedOutByPatron = None,
          onHoldTill = None,
        )
      case bookOnHold: BookOnHold =>
        RawBook(
          bookId = bookOnHold.bookId,
          bookType = bookOnHold.bookType,
          bookState = BookState.OnHold,
          availableAtBranch = None,
          onHoldAtBranch = Some(bookOnHold.holdPlacedAt),
          onHoldByPatron = Some(bookOnHold.byPatron),
          checkedOutAtBranch = None,
          checkedOutByPatron = None,
          onHoldTill = bookOnHold.holdTill,
        )
      case checkedOutBook: CheckedOutBook =>
        RawBook(
          bookId = checkedOutBook.bookId,
          bookType = checkedOutBook.bookType,
          bookState = BookState.CheckedOut,
          availableAtBranch = None,
          onHoldAtBranch = None,
          onHoldByPatron = None,
          checkedOutAtBranch = Some(checkedOutBook.checkedOutAt),
          checkedOutByPatron = Some(checkedOutBook.byPatron),
          onHoldTill = None,
        )
