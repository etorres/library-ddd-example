package es.eriktorr.library
package lending.application

import book.model.BookInstanceAddedToCatalogue
import lending.model.{AvailableBook, AvailableBooks}
import shared.infrastructure.EventHandler
import shared.infrastructure.KafkaClients.KafkaConsumerIO
import shared.refined.types.UUID

import cats.effect.IO
import fs2.Stream

final class CreateAvailableBookOnInstanceAdded(
    availableBooks: AvailableBooks,
    eventHandler: EventHandler[BookInstanceAddedToCatalogue],
    libraryBranchId: UUID,
):
  def handle: Stream[IO, Unit] = for _ <- eventHandler.handleWith { event =>
      val availableBook = AvailableBook.from(event.bookInstance, libraryBranchId)
      availableBooks.add(availableBook)
    }
  yield ()
