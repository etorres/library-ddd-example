package es.eriktorr.library
package lending.application

import lending.model.BookEvent.*
import lending.model.Books
import shared.infrastructure.EventHandler

import cats.effect.IO
import fs2.Stream

final class ReactToChangeOnBookState(
    books: Books,
    bookPlacedOnHoldEventHandler: EventHandler[BookPlacedOnHold],
    bookCheckedOutEventHandler: EventHandler[BookCheckedOut],
    bookHoldExpiredEventHandler: EventHandler[BookHoldExpired],
    bookHoldCanceledEventHandler: EventHandler[BookHoldCanceled],
    bookReturnedEventHandler: EventHandler[BookReturned],
):
  def handleBookPlacedOnHold: Stream[IO, Unit] = bookPlacedOnHoldEventHandler.handleWith { event =>
    for
      book <- books.findBy(event.bookId)
      _ <- book.fold(IO.unit) {
        ???
        ???
        ???
      }
    yield ()
  }

  /*
  @EventListener
      void handle(BookPlacedOnHold bookPlacedOnHold) {
          bookRepository.findBy(new BookId(bookPlacedOnHold.getBookId()))
                  .map(book -> handleBookPlacedOnHold(book, bookPlacedOnHold))
                  .map(this::saveBook);
      }

  private Book handleBookPlacedOnHold(Book book, BookPlacedOnHold bookPlacedOnHold) {
          return API.Match(book).of(
                  Case($(instanceOf(AvailableBook.class)), availableBook -> availableBook.handle(bookPlacedOnHold)),
                  Case($(instanceOf(BookOnHold.class)), bookOnHold -> raiseDuplicateHoldFoundEvent(bookOnHold, bookPlacedOnHold)),
                  Case($(), () -> book)
          );
      }

  private Book saveBook(Book book) {
          bookRepository.save(book);
          return book;
      }
   */

  def handleBookCheckedOut: Stream[IO, Unit] = for _ <- bookCheckedOutEventHandler.handleWith {
      event =>
        ???
        ???
        ???
    }
  yield ()

  def handleBookHoldExpired: Stream[IO, Unit] = for _ <- bookHoldExpiredEventHandler.handleWith {
      event =>
        ???
        ???
        ???
    }
  yield ()

  def handleBookHoldCanceled: Stream[IO, Unit] = for _ <- bookHoldCanceledEventHandler.handleWith {
      event =>
        ???
        ???
        ???
    }
  yield ()

  def handleBookReturned: Stream[IO, Unit] = for _ <- bookReturnedEventHandler.handleWith { event =>
      ???
      ???
      ???
    }
  yield ()
