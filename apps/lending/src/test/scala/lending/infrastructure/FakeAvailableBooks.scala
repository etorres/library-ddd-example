package es.eriktorr.library
package lending.infrastructure

import lending.infrastructure.FakeAvailableBooks.AvailableBooksState
import lending.model.{AvailableBook, AvailableBooks}
import shared.refined.types.UUID

import cats.effect.{IO, Ref}

final class FakeAvailableBooks(stateRef: Ref[IO, AvailableBooksState]) extends AvailableBooks:
  override def add(availableBook: AvailableBook): IO[Unit] =
    stateRef.update(currentState => currentState.copy(availableBook :: currentState.availableBooks))

  override def findBy(bookId: UUID): IO[Option[AvailableBook]] =
    stateRef.get.map(_.availableBooks.find(_.bookId == bookId))

object FakeAvailableBooks:
  final case class AvailableBooksState(availableBooks: List[AvailableBook]):
    def set(newAvailableBooks: List[AvailableBook]): AvailableBooksState =
      copy(availableBooks = newAvailableBooks)

  object AvailableBooksState:
    def empty: AvailableBooksState = AvailableBooksState(List.empty)
