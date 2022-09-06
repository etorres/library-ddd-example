package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookIdJdbcMapping
import lending.infrastructure.JdbcPatronsWriter.PatronJdbcMapping
import lending.model.Patron.{Hold, OverdueCheckout}
import lending.model.{Patron, PatronId}

import cats.effect.IO
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*

final class JdbcPatronsWriter(transactor: Transactor[IO]) extends PatronJdbcMapping:
  def save(patron: Patron): IO[Unit] = IO.unit <* sql"""
      INSERT INTO patrons (
        patron_id, patron_type
      ) VALUES (
        ${patron.patronId}, ${patron.patronType}
      )""".update.run.transact(transactor)

  def save(patronId: PatronId, hold: Hold): IO[Unit] = IO.unit <* sql"""
        INSERT INTO holds (
          book_id, patron_id, library_branch_id, till
        ) VALUES (
          ${hold.bookId}, $patronId, ${hold.libraryBranchId}, ${hold.till}
        )""".update.run.transact(transactor)

  def save(patronId: PatronId, overdueCheckout: OverdueCheckout): IO[Unit] = IO.unit <* sql"""
          INSERT INTO overdue_checkouts (
            book_id, patron_id, library_branch_id
          ) VALUES (
            ${overdueCheckout.bookId}, $patronId, ${overdueCheckout.libraryBranchId}
          )""".update.run.transact(transactor)

object JdbcPatronsWriter:
  trait PatronJdbcMapping
      extends BookIdJdbcMapping
      with LibraryBranchIdJdbcMapping
      with PatronIdJdbcMapping
      with PatronTypeJdbcMapping
