package es.eriktorr.library
package catalogue.infrastructure

import book.model.{BookInstance, BookInstanceAddedToCatalogue}
import catalogue.application.AddBookInstanceToCatalogue
import catalogue.infrastructure.FakeCatalogue.CatalogueState
import catalogue.model.Book
import shared.infrastructure.FakeClock.ClockState
import shared.infrastructure.FakeEventPublisher.EventPublisherState
import shared.infrastructure.FakeUUIDGen.UUIDGenState
import shared.infrastructure.{FakeClock, FakeEventPublisher, FakeUUIDGen}

import cats.effect.std.UUIDGen
import cats.effect.{Clock, IO, Ref}

import java.time.Instant
import java.util.UUID

object AddBookInstanceToCatalogueSuiteRunner:
  final case class AddBookInstanceToCatalogueState(
      catalogueState: CatalogueState,
      clockState: ClockState,
      eventPublisherState: EventPublisherState[BookInstanceAddedToCatalogue],
      uuidGeneratorState: UUIDGenState,
  ):
    def clearInstants: AddBookInstanceToCatalogueState = copy(clockState = ClockState.empty)

    def clearUUIDs: AddBookInstanceToCatalogueState =
      copy(uuidGeneratorState = UUIDGenState.empty)

    def setBooks(books: Map[Book, List[BookInstance]]): AddBookInstanceToCatalogueState =
      copy(catalogueState = catalogueState.set(books))

    def setEvents(events: List[BookInstanceAddedToCatalogue]): AddBookInstanceToCatalogueState =
      copy(eventPublisherState = eventPublisherState.set(events))

    def setInstants(instants: List[Instant]): AddBookInstanceToCatalogueState =
      copy(clockState = clockState.set(instants))

    def setUUIDs(uuids: List[UUID]): AddBookInstanceToCatalogueState =
      copy(uuidGeneratorState = uuidGeneratorState.set(uuids))

  object AddBookInstanceToCatalogueState:
    def empty: AddBookInstanceToCatalogueState = AddBookInstanceToCatalogueState(
      CatalogueState.empty,
      ClockState.empty,
      EventPublisherState.empty[BookInstanceAddedToCatalogue],
      UUIDGenState.empty,
    )

  def runWith[A](initialState: AddBookInstanceToCatalogueState)(
      run: AddBookInstanceToCatalogue => IO[A],
  ): IO[(Either[Throwable, A], AddBookInstanceToCatalogueState)] = for
    catalogueStateRef <- Ref.of[IO, CatalogueState](initialState.catalogueState)
    clockStateRef <- Ref.of[IO, ClockState](initialState.clockState)
    eventPublisherStateRef <- Ref.of[IO, EventPublisherState[BookInstanceAddedToCatalogue]](
      initialState.eventPublisherState,
    )
    uuidGeneratorStateRef <- Ref.of[IO, UUIDGenState](initialState.uuidGeneratorState)
    catalogue = FakeCatalogue(catalogueStateRef)
    eventPublisher = FakeEventPublisher(eventPublisherStateRef)
    addBookInstanceToCatalogue = {
      given Clock[IO] = FakeClock(clockStateRef)
      given UUIDGen[IO] = FakeUUIDGen(uuidGeneratorStateRef)
      AddBookInstanceToCatalogue(catalogue, eventPublisher)
    }
    result <- run(addBookInstanceToCatalogue).attempt
    finalCatalogueState <- catalogueStateRef.get
    finalClockState <- clockStateRef.get
    finalEventPublisherState <- eventPublisherStateRef.get
    finalUuidGeneratorState <- uuidGeneratorStateRef.get
    finalState = initialState.copy(
      catalogueState = finalCatalogueState,
      clockState = finalClockState,
      eventPublisherState = finalEventPublisherState,
      uuidGeneratorState = finalUuidGeneratorState,
    )
  yield (result, finalState)
