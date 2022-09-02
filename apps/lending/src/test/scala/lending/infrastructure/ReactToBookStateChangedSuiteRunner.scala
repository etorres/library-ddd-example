package es.eriktorr.library
package lending.infrastructure

import lending.application.ReactToBookStateChanged
import lending.infrastructure.FakeBooks.BooksState
import lending.model.{Book, BookDuplicateHoldFound, BookStateChanged}
import shared.infrastructure.FakeEventHandler.EventHandlerState
import shared.infrastructure.FakeEventPublisher.EventPublisherState
import shared.infrastructure.{FakeEventHandler, FakeEventPublisher}

import cats.effect.{IO, Ref}

object ReactToBookStateChangedSuiteRunner:
  final case class ReactToBookStateChangedState(
      booksState: BooksState,
      bookStateChangedEvenHandlerState: EventHandlerState[BookStateChanged],
      bookDuplicateHoldFoundEventPublisherState: EventPublisherState[BookDuplicateHoldFound],
  ):
    def clearEvents: ReactToBookStateChangedState =
      copy(bookStateChangedEvenHandlerState = EventHandlerState.empty)
    def setBooks(books: List[Book]): ReactToBookStateChangedState =
      copy(booksState = booksState.set(books))
    def setEvents(events: List[BookStateChanged]): ReactToBookStateChangedState =
      copy(bookStateChangedEvenHandlerState = bookStateChangedEvenHandlerState.set(events))

  object ReactToBookStateChangedState:
    def empty: ReactToBookStateChangedState = ReactToBookStateChangedState(
      BooksState.empty,
      EventHandlerState.empty,
      EventPublisherState.empty,
    )

  def runWith[A](initialState: ReactToBookStateChangedState)(
      run: ReactToBookStateChanged => IO[A],
  ): IO[(Either[Throwable, A], ReactToBookStateChangedState)] = for
    booksStateRef <- Ref.of[IO, BooksState](initialState.booksState)
    bookStateChangedEvenHandlerStateRef <- Ref.of[IO, EventHandlerState[BookStateChanged]](
      initialState.bookStateChangedEvenHandlerState,
    )
    bookDuplicateHoldFoundEventPublisherStateRef <- Ref
      .of[IO, EventPublisherState[BookDuplicateHoldFound]](
        initialState.bookDuplicateHoldFoundEventPublisherState,
      )
    books = FakeBooks(booksStateRef)
    bookStateChangedEvenHandler = FakeEventHandler(bookStateChangedEvenHandlerStateRef)
    bookDuplicateHoldFoundEventPublisher = FakeEventPublisher(
      bookDuplicateHoldFoundEventPublisherStateRef,
    )
    reactToBookStateChanged = ReactToBookStateChanged(
      books,
      bookStateChangedEvenHandler,
      bookDuplicateHoldFoundEventPublisher,
    )
    result <- run(reactToBookStateChanged).attempt
    finalBooksState <- booksStateRef.get
    finalBookStateChangedEvenHandlerState <- bookStateChangedEvenHandlerStateRef.get
    finalBookDuplicateHoldFoundEventPublisherState <-
      bookDuplicateHoldFoundEventPublisherStateRef.get
    finalState = initialState.copy(
      booksState = finalBooksState,
      bookStateChangedEvenHandlerState = finalBookStateChangedEvenHandlerState,
      bookDuplicateHoldFoundEventPublisherState = finalBookDuplicateHoldFoundEventPublisherState,
    )
  yield (result, finalState)
