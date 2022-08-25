package es.eriktorr.library
package catalogue.infrastructure

import book.infrastructure.{AuthorJdbcMapping, ISBNJdbcMapping, TitleJdbcMapping}
import book.model.{Book, BookInstance, ISBN}
import catalogue.model.Catalogue
import shared.refined.types.infrastructure.UUIDJdbcMapping

import cats.effect.IO
import doobie.Transactor
import doobie.implicits.*

final class JdbcCatalogue(transactor: Transactor[IO])
    extends Catalogue
    with AuthorJdbcMapping
    with ISBNJdbcMapping
    with TitleJdbcMapping
    with UUIDJdbcMapping:
  override def add(book: Book): IO[Unit] = IO.unit <* sql"""
           INSERT INTO book_catalogue (isbn, title, author) 
           VALUES (
             ${book.isbn},
             ${book.title},
             ${book.author}
           )""".update.run.transact(transactor)

  override def add(bookInstance: BookInstance): IO[Unit] = IO.unit <* sql"""
           INSERT INTO book_instance_catalogue (book_id, isbn) 
           VALUES (
             ${bookInstance.bookId},
             ${bookInstance.isbn}
           )""".update.run.transact(transactor)

  override def findBy(isbn: ISBN): IO[Option[Book]] = sql"""
           SELECT isbn, title, author 
           FROM book_catalogue 
           WHERE
             isbn = $isbn""".query[Book].option.transact(transactor)
