package es.eriktorr.library
package lending.application

import book.model.BookInstanceAddedToCatalogue
import lending.model.{Books, LibraryBranchId}
import lending.model.Book.AvailableBook
import shared.infrastructure.EventHandler
import shared.infrastructure.KafkaClients.KafkaConsumerIO

import cats.effect.IO
import fs2.Stream

final class CreateAvailableBookOnInstanceAdded(
    books: Books,
    eventHandler: EventHandler[BookInstanceAddedToCatalogue],
    libraryBranchId: LibraryBranchId,
):
  def handle: Stream[IO, Unit] = for _ <- eventHandler.handleWith { event =>
      val availableBook = AvailableBook.from(event.bookInstance, libraryBranchId)
      books.save(availableBook)
    }
  yield ()
