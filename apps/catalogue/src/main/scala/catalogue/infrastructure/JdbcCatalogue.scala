package es.eriktorr.library
package catalogue.infrastructure

import book.model.{Book, BookInstance}
import catalogue.model.Catalogue

import cats.effect.IO
import doobie.Transactor
import doobie.implicits.*

final class JdbcCatalogue(transactor: Transactor[IO])
    extends Catalogue
    with AuthorMapping
    with ISBNMapping
    with TitleMapping:
  override def add(book: Book): IO[Unit] = IO.unit <* sql"""
         INSERT INTO catalogue_book (isbn, title, author) 
         VALUES (
           ${book.isbn},
           ${book.title},
           ${book.author}
         )""".update.run.transact(transactor)

  override def add(bookInstance: BookInstance): IO[Unit] = ???
