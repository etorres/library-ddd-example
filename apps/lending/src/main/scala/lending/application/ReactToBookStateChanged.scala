package es.eriktorr.library
package lending.application

import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import lending.model.BookStateChanged.*
import lending.model.{BookDuplicateHoldFound, Books, BookStateChanged}
import shared.EventId
import shared.infrastructure.{EventHandler, EventPublisher}

import cats.effect.std.UUIDGen
import cats.effect.{Clock, IO}
import fs2.Stream

final class ReactToBookStateChanged(
    books: Books,
    bookStateChangedEvenHandler: EventHandler[BookStateChanged],
    bookDuplicateHoldFoundEventPublisher: EventPublisher[BookDuplicateHoldFound],
)(using clock: Clock[IO], uuidGenerator: UUIDGen[IO]):

  def handle: Stream[IO, Unit] = bookStateChangedEvenHandler.handleWith { event =>
    event match
      case bookCheckedOut: BookCheckedOut => handle(bookCheckedOut)
      case bookHoldCanceled: BookHoldCanceled => handle(bookHoldCanceled)
      case bookHoldExpired: BookHoldExpired => handle(bookHoldExpired)
      case bookPlacedOnHold: BookPlacedOnHold => handle(bookPlacedOnHold)
      case bookReturned: BookReturned => handle(bookReturned)
  }

  private[this] def handle(bookCheckedOut: BookCheckedOut) = for
    currentBook <- books.findBy(bookCheckedOut.bookId)
    _ <- currentBook.fold(IO.unit) { book =>
      book match
        case _: AvailableBook => IO.unit
        case _: BookOnHold => books.save(CheckedOutBook.from(bookCheckedOut))
        case _: CheckedOutBook => IO.unit
    }
  yield ()

  private[this] def handle(bookHoldCanceled: BookHoldCanceled) = for
    currentBook <- books.findBy(bookHoldCanceled.bookId)
    _ <- currentBook.fold(IO.unit) { book =>
      book match
        case _: AvailableBook => IO.unit
        case bookOnHold: BookOnHold =>
          books.save(AvailableBook.from(bookOnHold, bookHoldCanceled))
        case _: CheckedOutBook => IO.unit
    }
  yield ()

  private[this] def handle(bookHoldExpired: BookHoldExpired) = for
    currentBook <- books.findBy(bookHoldExpired.bookId)
    _ <- currentBook.fold(IO.unit) { book =>
      book match
        case _: AvailableBook => IO.unit
        case bookOnHold: BookOnHold =>
          books.save(AvailableBook.from(bookOnHold, bookHoldExpired))
        case _: CheckedOutBook => IO.unit
    }
  yield ()

  private[this] def handle(bookPlacedOnHold: BookPlacedOnHold) = for
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

  private[this] def handle(bookReturned: BookReturned) = for
    currentBook <- books.findBy(bookReturned.bookId)
    _ <- currentBook.fold(IO.unit) { book =>
      book match
        case _: AvailableBook => IO.unit
        case bookOnHold: BookOnHold => books.save(AvailableBook.from(bookOnHold, bookReturned))
        case _: CheckedOutBook => IO.unit
    }
  yield ()

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
