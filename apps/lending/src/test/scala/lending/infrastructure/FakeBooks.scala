package es.eriktorr.library
package lending.infrastructure

import book.model.BookId
import lending.infrastructure.FakeBooks.BooksState
import lending.model.{Book, Books}

import cats.effect.{IO, Ref}

final class FakeBooks(stateRef: Ref[IO, BooksState]) extends Books:
  override def save(book: Book): IO[Unit] =
    stateRef.update(currentState => currentState.copy(currentState.books + (book.bookId -> book)))

  override def findBy(bookId: BookId): IO[Option[Book]] =
    stateRef.get.map(_.books.get(bookId))

object FakeBooks:
  final case class BooksState(books: Map[BookId, Book]):
    def set(newBooks: List[Book]): BooksState =
      copy(books = newBooks.map(x => x.bookId -> x).toMap)

  object BooksState:
    def empty: BooksState = BooksState(Map.empty)
