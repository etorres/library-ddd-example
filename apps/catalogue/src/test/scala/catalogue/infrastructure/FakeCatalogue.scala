package es.eriktorr.library
package catalogue.infrastructure

import book.model.{Book, BookInstance, ISBN}
import catalogue.infrastructure.FakeCatalogue.CatalogueState
import catalogue.model.Catalogue

import cats.effect.{IO, Ref}

final class FakeCatalogue(stateRef: Ref[IO, CatalogueState]) extends Catalogue:
  override def add(book: Book): IO[Unit] =
    stateRef.update(currentState =>
      currentState.copy(currentState.books.updatedWith(book) {
        case Some(bookInstances) => Some(bookInstances)
        case None => Some(List.empty)
      }),
    )

  override def add(bookInstance: BookInstance): IO[Unit] = stateRef.update { currentState =>
    currentState.books
      .find { case (book, _) => book.isbn == bookInstance.isbn }
      .fold(currentState) { case (book, bookInstances) =>
        currentState.copy(currentState.books + (book -> (bookInstance :: bookInstances)))
      }
  }

  override def findBy(isbn: ISBN): IO[Option[Book]] =
    stateRef.get.map(_.books.keys.find(_.isbn == isbn))

object FakeCatalogue:
  final case class CatalogueState(books: Map[Book, List[BookInstance]]):
    def set(newBooks: Map[Book, List[BookInstance]]): CatalogueState = copy(books = newBooks)

  object CatalogueState:
    def empty: CatalogueState = CatalogueState(Map.empty)
