package es.eriktorr.library
package lending.infrastructure

import lending.model.{AvailableBook, AvailableBooks}
import shared.refined.types.UUID

import cats.effect.IO
import doobie.Transactor

final class JdbcAvailableBooks(transactor: Transactor[IO]) extends AvailableBooks:
  override def add(availableBook: AvailableBook): IO[Unit] = ???

  override def findBy(bookId: UUID): IO[Option[AvailableBook]] = ???
