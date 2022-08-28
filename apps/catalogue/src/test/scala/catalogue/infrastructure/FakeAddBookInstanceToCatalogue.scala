package es.eriktorr.library
package catalogue.infrastructure

import book.model.BookInstanceAddedToCatalogue
import catalogue.application.AddBookInstanceToCatalogue

import cats.effect.{IO, Ref}

final case class AddBookInstanceToCatalogueState(
    catalogueState: CatalogueState,
    eventPublisherState: EventPublisherState[BookInstanceAddedToCatalogue],
)

object AddBookInstanceToCatalogueState

object FakeAddBookInstanceToCatalogue:
  def withState[A](initialState: AddBookInstanceToCatalogueState)(
      run: AddBookInstanceToCatalogue => IO[A],
  ): IO[(Either[Throwable, A], AddBookInstanceToCatalogueState)] = for
    catalogueStateRef <- Ref.of[IO, CatalogueState](initialState.catalogueState)
    eventPublisherStateRef <- Ref.of[IO, EventPublisherState[BookInstanceAddedToCatalogue]](
      initialState.eventPublisherState,
    )
    catalogue = FakeCatalogue(catalogueStateRef)
    eventPublisher = FakeEventPublisher(eventPublisherStateRef)
    addBookInstanceToCatalogue = AddBookInstanceToCatalogue(catalogue, eventPublisher)
    result <- run(addBookInstanceToCatalogue).attempt
    finalCatalogueState <- catalogueStateRef.get
    finalEventPublisherState <- eventPublisherStateRef.get
    finalState = initialState.copy(
      catalogueState = finalCatalogueState,
      eventPublisherState = finalEventPublisherState,
    )
  yield (result, finalState)
