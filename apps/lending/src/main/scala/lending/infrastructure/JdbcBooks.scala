package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.{BookIdJdbcMapping, BookTypeJdbcMapping}
import book.model.{BookId, BookType}
import lending.infrastructure.LibraryBranchIdJdbcMapping
import lending.model.*
import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import lending.model.BookState.{Available, CheckedOut, OnHold}

import cats.effect.IO
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.Instant
import java.util.UUID

final class JdbcBooks(transactor: Transactor[IO])
    extends Books
    with BookIdJdbcMapping
    with BookStateJdbcMapping
    with BookTypeJdbcMapping
    with LibraryBranchIdJdbcMapping
    with PatronIdJdbcMapping:
  override def add(book: Book): IO[Unit] = book match
    case AvailableBook(bookId, bookType, libraryBranchId) =>
      IO.unit <* sql"""
        INSERT INTO books (
          book_id, 
          book_type, 
          book_state, 
          available_at_branch
        ) VALUES (
          $bookId,
          $bookType,
          ${BookState.Available},
          $libraryBranchId
        )""".stripMargin.update.run.transact(transactor)
    case _ => IO.unit

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
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
    .query[
      (
          BookId,
          BookType,
          BookState,
          Option[LibraryBranchId],
          Option[LibraryBranchId],
          Option[PatronId],
          Option[LibraryBranchId],
          Option[PatronId],
          Option[Instant],
      ),
    ]
    .option
    .transact(transactor)
    .map {
      case Some(
            (
              bookId,
              bookType,
              bookState,
              availableAtBranch,
              onHoldAtBranch,
              onHoldByPatron,
              checkedOutAtBranch,
              checkedOutByPatron,
              onHoldTill,
            ),
          ) =>
        bookState match
          case Available =>
            Some(
              AvailableBook(
                bookId,
                bookType,
                availableAtBranch.getOrElse(
                  throw IllegalStateException(
                    s"Missing library branch Id for available book: $bookId",
                  ),
                ),
              ),
            )
          case CheckedOut =>
            Some(
              CheckedOutBook(
                bookId,
                bookType,
                checkedOutAtBranch.getOrElse(
                  throw IllegalStateException(
                    s"Missing library branch for book on hold: $bookId",
                  ),
                ),
                checkedOutByPatron.getOrElse(
                  throw IllegalStateException(
                    s"Missing patron for book on hold: $bookId",
                  ),
                ),
              ),
            )
          case OnHold =>
            Some(
              BookOnHold(
                bookId,
                bookType,
                onHoldAtBranch.getOrElse(
                  throw IllegalStateException(
                    s"Missing library branch for book on hold: $bookId",
                  ),
                ),
                onHoldByPatron.getOrElse(
                  throw IllegalStateException(
                    s"Missing patron for book on hold: $bookId",
                  ),
                ),
                onHoldTill,
              ),
            )
      case None => None
    }
