package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookTypeJdbcMapping
import lending.model.{AvailableBook, AvailableBooks, BookState}
import shared.refined.types.UUID
import shared.refined.types.infrastructure.UUIDJdbcMapping

import cats.effect.IO
import doobie.Transactor
import doobie.implicits.*

final class JdbcAvailableBooks(transactor: Transactor[IO])
    extends AvailableBooks
    with BookStateJdbcMapping
    with BookTypeJdbcMapping
    with UUIDJdbcMapping:
  override def add(availableBook: AvailableBook): IO[Unit] = IO.unit <* sql"""
           INSERT INTO available_books (book_id, book_type, book_state, available_at_branch)
           VALUES (
             ${availableBook.bookId},
             ${availableBook.bookType},
             ${BookState.Available},
             ${availableBook.libraryBranchId}
           )""".update.run.transact(transactor)

  override def findBy(bookId: UUID): IO[Option[AvailableBook]] = sql"""
             SELECT book_id, book_type, available_at_branch 
             FROM available_books 
             WHERE
               book_id = $bookId""".query[AvailableBook].option.transact(transactor)
