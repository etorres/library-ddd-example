package es.eriktorr.library
package lending.infrastructure

import book.model.BookInstanceAddedToCatalogue
import lending.application.CreateAvailableBookOnInstanceAdded
import lending.infrastructure.FakeBooks
import lending.infrastructure.FakeBooks.BooksState
import lending.model.{Book, LibraryBranchId}
import shared.infrastructure.FakeEventHandler
import shared.infrastructure.FakeEventHandler.EventHandlerState

import cats.effect.{IO, Ref}

object CreateAvailableBookOnInstanceAddedSuiteRunner:
  final case class CreateAvailableBookOnInstanceAddedState(
      booksState: BooksState,
      eventHandlerState: EventHandlerState[BookInstanceAddedToCatalogue],
      libraryBranchId: LibraryBranchId,
  ):
    def clearEvents: CreateAvailableBookOnInstanceAddedState =
      copy(eventHandlerState = EventHandlerState.empty)
    def setBooks(books: List[Book]): CreateAvailableBookOnInstanceAddedState =
      copy(booksState = booksState.set(books))
    def setEvents(
        events: List[BookInstanceAddedToCatalogue],
    ): CreateAvailableBookOnInstanceAddedState =
      copy(eventHandlerState = eventHandlerState.set(events))

  object CreateAvailableBookOnInstanceAddedState:
    def from(libraryBranchId: LibraryBranchId): CreateAvailableBookOnInstanceAddedState =
      CreateAvailableBookOnInstanceAddedState(
        BooksState.empty,
        EventHandlerState.empty,
        libraryBranchId,
      )

  def runWith[A](
      initialState: CreateAvailableBookOnInstanceAddedState,
  )(
      run: CreateAvailableBookOnInstanceAdded => IO[A],
  ): IO[(Either[Throwable, A], CreateAvailableBookOnInstanceAddedState)] = for
    booksStateRef <- Ref.of[IO, BooksState](initialState.booksState)
    eventHandlerStateRef <- Ref.of[IO, EventHandlerState[BookInstanceAddedToCatalogue]](
      initialState.eventHandlerState,
    )
    books = FakeBooks(booksStateRef)
    eventHandler = FakeEventHandler(eventHandlerStateRef)
    createAvailableBookOnInstanceAdded = CreateAvailableBookOnInstanceAdded(
      books,
      eventHandler,
      initialState.libraryBranchId,
    )
    result <- run(createAvailableBookOnInstanceAdded).attempt
    finalBooksState <- booksStateRef.get
    finalEventHandlerState <- eventHandlerStateRef.get
    finalState = initialState.copy(
      booksState = finalBooksState,
      eventHandlerState = finalEventHandlerState,
    )
  yield (result, finalState)
