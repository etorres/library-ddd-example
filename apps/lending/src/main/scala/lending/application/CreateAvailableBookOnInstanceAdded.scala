package es.eriktorr.library
package lending.application

import lending.model.{AvailableBook, AvailableBooks, BookInstanceAddedToCatalogueEventHandler}
import shared.infrastructure.KafkaClients.KafkaConsumerIO
import shared.refined.types.UUID

import cats.effect.IO
import fs2.Stream

final class CreateAvailableBookOnInstanceAdded(
    availableBooks: AvailableBooks,
    bookInstanceAddedToCatalogueEventHandler: BookInstanceAddedToCatalogueEventHandler,
    libraryBranchId: UUID,
):
  def handle: Stream[IO, Unit] = for _ <- bookInstanceAddedToCatalogueEventHandler.handleWith {
      event =>
        val availableBook = AvailableBook.from(event.bookInstance, libraryBranchId)
        availableBooks.add(availableBook)
    }
  yield ()
