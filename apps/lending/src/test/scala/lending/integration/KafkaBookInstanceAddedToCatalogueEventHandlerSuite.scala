package es.eriktorr.library
package lending.integration

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.infrastructure.BookInstanceGenerators.bookInstanceAddedToCatalogueGen
import book.model.BookInstanceAddedToCatalogue
import lending.infrastructure.KafkaBookInstanceAddedToCatalogueEventHandler
import lending.integration.KafkaBookInstanceAddedToCatalogueEventHandlerSuite.{
  bookInstanceAddedToCatalogueAvroCodec,
  logger,
}
import shared.infrastructure.KafkaClientsSuite.KafkaEventHandlerSuite
import shared.infrastructure.KafkaTestConfig

import cats.effect.IO

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class KafkaBookInstanceAddedToCatalogueEventHandlerSuite
    extends KafkaEventHandlerSuite[BookInstanceAddedToCatalogue]:
  override def kafkaTestConfig: KafkaTestConfig = KafkaTestConfig.LendingBookInstances

  test("should handle new book instance added event in a kafka topic") {
    checkUsing(bookInstanceAddedToCatalogueGen, KafkaBookInstanceAddedToCatalogueEventHandler(_))
  }

object KafkaBookInstanceAddedToCatalogueEventHandlerSuite
    extends BookInstanceAddedToCatalogueAvroCodec:
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
