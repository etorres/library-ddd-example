package es.eriktorr.library
package catalogue.infrastructure

import book.model.{Book, BookInstance, ISBN}
import catalogue.model.Catalogue

import cats.effect.{IO, Ref}

final case class CatalogueState(books: Map[Book, BookInstance])

object CatalogueState:
  def empty: CatalogueState = CatalogueState(Map.empty)

final class FakeCatalogue(stateRef: Ref[IO, CatalogueState]) extends Catalogue:
  override def add(book: Book): IO[Unit] = ???

  override def add(bookInstance: BookInstance): IO[Unit] = ???

  override def findBy(isbn: ISBN): IO[Option[Book]] = ???
