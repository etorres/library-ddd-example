package es.eriktorr.library
package lending.infrastructure

import book.model.BookId
import lending.infrastructure.FakeBooks.BooksState
import lending.model.{Book, Books}

import cats.effect.{IO, Ref}

final class FakeBooks(stateRef: Ref[IO, BooksState]) extends Books:
  override def save(book: Book): IO[Unit] =
    stateRef.update(currentState => currentState.copy(book :: currentState.books))

  override def findBy(bookId: BookId): IO[Option[Book]] =
    stateRef.get.map(_.books.find(_.bookId == bookId))

object FakeBooks:
  final case class BooksState(books: List[Book]):
    def set(newBooks: List[Book]): BooksState =
      copy(books = newBooks)

  object BooksState:
    def empty: BooksState = BooksState(List.empty)
