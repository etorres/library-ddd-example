package es.eriktorr.library
package lending.infrastructure

import book.model.BookInstanceAddedToCatalogue
import lending.application.CreateAvailableBookOnInstanceAdded
import lending.infrastructure.FakeAvailableBooks
import lending.infrastructure.FakeAvailableBooks.AvailableBooksState
import lending.model.AvailableBook
import shared.infrastructure.FakeEventHandler
import shared.infrastructure.FakeEventHandler.EventHandlerState
import shared.refined.types.UUID

import cats.effect.{IO, Ref}

object CreateAvailableBookOnInstanceAddedSuiteRunner:
  final case class CreateAvailableBookOnInstanceAddedState(
      availableBooksState: AvailableBooksState,
      eventHandlerState: EventHandlerState[BookInstanceAddedToCatalogue],
      libraryBranchId: UUID,
  ):
    def clearEvents: CreateAvailableBookOnInstanceAddedState =
      copy(eventHandlerState = EventHandlerState.empty)
    def setAvailableBooks(
        availableBooks: List[AvailableBook],
    ): CreateAvailableBookOnInstanceAddedState =
      copy(availableBooksState = availableBooksState.set(availableBooks))
    def setEvents(
        events: List[BookInstanceAddedToCatalogue],
    ): CreateAvailableBookOnInstanceAddedState =
      copy(eventHandlerState = eventHandlerState.set(events))

  object CreateAvailableBookOnInstanceAddedState:
    def from(libraryBranchId: UUID): CreateAvailableBookOnInstanceAddedState =
      CreateAvailableBookOnInstanceAddedState(
        AvailableBooksState.empty,
        EventHandlerState.empty,
        libraryBranchId,
      )

  def runWith[A](
      initialState: CreateAvailableBookOnInstanceAddedState,
  )(
      run: CreateAvailableBookOnInstanceAdded => IO[A],
  ): IO[(Either[Throwable, A], CreateAvailableBookOnInstanceAddedState)] = for
    availableBooksStateRef <- Ref.of[IO, AvailableBooksState](initialState.availableBooksState)
    eventHandlerStateRef <- Ref.of[IO, EventHandlerState[BookInstanceAddedToCatalogue]](
      initialState.eventHandlerState,
    )
    availableBooks = FakeAvailableBooks(availableBooksStateRef)
    eventHandler = FakeEventHandler(eventHandlerStateRef)
    createAvailableBookOnInstanceAdded = CreateAvailableBookOnInstanceAdded(
      availableBooks,
      eventHandler,
      initialState.libraryBranchId,
    )
    result <- run(createAvailableBookOnInstanceAdded).attempt
    finalAvailableBooksState <- availableBooksStateRef.get
    finalEventHandlerState <- eventHandlerStateRef.get
    finalState = initialState.copy(
      availableBooksState = finalAvailableBooksState,
      eventHandlerState = finalEventHandlerState,
    )
  yield (result, finalState)
