package es.eriktorr.library
package lending.application

import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import lending.model.BookStateChange.*
import lending.model.{BookDuplicateHoldFound, Books}
import shared.EventId
import shared.infrastructure.{EventHandler, EventPublisher}

import cats.effect.std.UUIDGen
import cats.effect.{Clock, IO}
import fs2.Stream

final class ReactToChangeOnBookState(
    books: Books,
    bookPlacedOnHoldEventHandler: EventHandler[BookPlacedOnHold],
    bookCheckedOutEventHandler: EventHandler[BookCheckedOut],
    bookHoldExpiredEventHandler: EventHandler[BookHoldExpired],
    bookHoldCanceledEventHandler: EventHandler[BookHoldCanceled],
    bookReturnedEventHandler: EventHandler[BookReturned],
    bookDuplicateHoldFoundEventPublisher: EventPublisher[BookDuplicateHoldFound],
)(using clock: Clock[IO], uuidGenerator: UUIDGen[IO]):
  def handleBookPlacedOnHold: Stream[IO, Unit] = bookPlacedOnHoldEventHandler.handleWith {
    bookPlacedOnHold =>
      for
        currentBook <- books.findBy(bookPlacedOnHold.bookId)
        _ <- currentBook.fold(IO.unit) { book =>
          book match
            case _: AvailableBook => books.save(BookOnHold.from(bookPlacedOnHold))
            case bookOnHold: BookOnHold =>
              if bookOnHold.byPatron != bookPlacedOnHold.patronId then
                raiseDuplicateHoldFoundEvent(bookOnHold, bookPlacedOnHold)
              else IO.unit
            case _: CheckedOutBook => IO.unit
        }
      yield ()
  }

  def handleBookCheckedOut: Stream[IO, Unit] = bookCheckedOutEventHandler.handleWith {
    bookCheckedOut =>
      for
        currentBook <- books.findBy(bookCheckedOut.bookId)
        _ <- currentBook.fold(IO.unit) { book =>
          book match
            case _: AvailableBook => IO.unit
            case _: BookOnHold => books.save(CheckedOutBook.from(bookCheckedOut))
            case _: CheckedOutBook => IO.unit
        }
      yield ()
  }

  def handleBookHoldExpired: Stream[IO, Unit] = bookHoldExpiredEventHandler.handleWith {
    bookHoldExpired =>
      for
        currentBook <- books.findBy(bookHoldExpired.bookId)
        _ <- currentBook.fold(IO.unit) { book =>
          book match
            case _: AvailableBook => IO.unit
            case bookOnHold: BookOnHold =>
              books.save(AvailableBook.from(bookOnHold, bookHoldExpired))
            case _: CheckedOutBook => IO.unit
        }
      yield ()
  }

  def handleBookHoldCanceled: Stream[IO, Unit] = bookHoldCanceledEventHandler.handleWith {
    bookHoldCanceled =>
      for
        currentBook <- books.findBy(bookHoldCanceled.bookId)
        _ <- currentBook.fold(IO.unit) { book =>
          book match
            case _: AvailableBook => IO.unit
            case bookOnHold: BookOnHold =>
              books.save(AvailableBook.from(bookOnHold, bookHoldCanceled))
            case _: CheckedOutBook => IO.unit
        }
      yield ()
  }

  def handleBookReturned: Stream[IO, Unit] = bookReturnedEventHandler.handleWith { bookReturned =>
    for
      currentBook <- books.findBy(bookReturned.bookId)
      _ <- currentBook.fold(IO.unit) { book =>
        book match
          case _: AvailableBook => IO.unit
          case bookOnHold: BookOnHold => books.save(AvailableBook.from(bookOnHold, bookReturned))
          case _: CheckedOutBook => IO.unit
      }
    yield ()
  }

  private[this] def raiseDuplicateHoldFoundEvent(
      bookOnHold: BookOnHold,
      bookPlacedOnHold: BookPlacedOnHold,
  ) = for
    eventId <- uuidGenerator.randomUUID.map(EventId.from)
    when <- clock.realTimeInstant
    _ <- bookDuplicateHoldFoundEventPublisher.publish(
      BookDuplicateHoldFound.from(eventId, when, bookOnHold, bookPlacedOnHold),
    )
  yield ()
