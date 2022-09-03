package es.eriktorr.library
package lending.infrastructure

import lending.application.ReactToBookStateChanged
import lending.infrastructure.FakeBooks.BooksState
import lending.model.{Book, BookDuplicateHoldFound, BookStateChanged}
import shared.infrastructure.FakeClock.ClockState
import shared.infrastructure.FakeEventHandler.EventHandlerState
import shared.infrastructure.FakeEventPublisher.EventPublisherState
import shared.infrastructure.FakeUUIDGen.UUIDGenState
import shared.infrastructure.{FakeClock, FakeEventHandler, FakeEventPublisher, FakeUUIDGen}

import cats.effect.std.UUIDGen
import cats.effect.{Clock, IO, Ref}

import java.time.Instant
import java.util.UUID

object ReactToBookStateChangedSuiteRunner:
  final case class ReactToBookStateChangedState(
      booksState: BooksState,
      bookStateChangedEvenHandlerState: EventHandlerState[BookStateChanged],
      bookDuplicateHoldFoundEventPublisherState: EventPublisherState[BookDuplicateHoldFound],
      clockState: ClockState,
      uuidGeneratorState: UUIDGenState,
  ):
    def clearEvents: ReactToBookStateChangedState =
      copy(bookStateChangedEvenHandlerState = EventHandlerState.empty)

    def clearInstants: ReactToBookStateChangedState = copy(clockState = ClockState.empty)

    def clearUUIDs: ReactToBookStateChangedState = copy(uuidGeneratorState = UUIDGenState.empty)

    def setBooks(books: List[Book]): ReactToBookStateChangedState =
      copy(booksState = booksState.set(books))

    def setEvents(events: List[BookStateChanged]): ReactToBookStateChangedState =
      copy(bookStateChangedEvenHandlerState = bookStateChangedEvenHandlerState.set(events))

    def setErrors(errors: List[BookDuplicateHoldFound]): ReactToBookStateChangedState =
      copy(bookDuplicateHoldFoundEventPublisherState =
        bookDuplicateHoldFoundEventPublisherState.set(errors),
      )

    def setInstants(instants: List[Instant]): ReactToBookStateChangedState =
      copy(clockState = clockState.set(instants))

    def setUUIDs(uuids: List[UUID]): ReactToBookStateChangedState =
      copy(uuidGeneratorState = uuidGeneratorState.set(uuids))

  object ReactToBookStateChangedState:
    def empty: ReactToBookStateChangedState = ReactToBookStateChangedState(
      BooksState.empty,
      EventHandlerState.empty,
      EventPublisherState.empty,
      ClockState.empty,
      UUIDGenState.empty,
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
    clockStateRef <- Ref.of[IO, ClockState](initialState.clockState)
    uuidGeneratorStateRef <- Ref.of[IO, UUIDGenState](initialState.uuidGeneratorState)
    books = FakeBooks(booksStateRef)
    bookStateChangedEvenHandler = FakeEventHandler(bookStateChangedEvenHandlerStateRef)
    bookDuplicateHoldFoundEventPublisher = FakeEventPublisher(
      bookDuplicateHoldFoundEventPublisherStateRef,
    )
    reactToBookStateChanged = {
      given Clock[IO] = FakeClock(clockStateRef)
      given UUIDGen[IO] = FakeUUIDGen(uuidGeneratorStateRef)
      ReactToBookStateChanged(
        books,
        bookStateChangedEvenHandler,
        bookDuplicateHoldFoundEventPublisher,
      )
    }
    result <- run(reactToBookStateChanged).attempt
    finalBooksState <- booksStateRef.get
    finalBookStateChangedEvenHandlerState <- bookStateChangedEvenHandlerStateRef.get
    finalBookDuplicateHoldFoundEventPublisherState <-
      bookDuplicateHoldFoundEventPublisherStateRef.get
    finalClockState <- clockStateRef.get
    finalUuidGeneratorState <- uuidGeneratorStateRef.get
    finalState = initialState.copy(
      booksState = finalBooksState,
      bookStateChangedEvenHandlerState = finalBookStateChangedEvenHandlerState,
      bookDuplicateHoldFoundEventPublisherState = finalBookDuplicateHoldFoundEventPublisherState,
      clockState = finalClockState,
      uuidGeneratorState = finalUuidGeneratorState,
    )
  yield (result, finalState)
