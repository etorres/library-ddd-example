package es.eriktorr.library
package catalogue.integration

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.infrastructure.BookInstanceGenerators.bookInstanceAddedToCatalogueGen
import book.model.BookInstanceAddedToCatalogue
import catalogue.infrastructure.KafkaBookInstanceAddedToCatalogueEventPublisher
import catalogue.integration.KafkaBookInstanceAddedToCatalogueEventPublisherSuite.{
  bookInstanceAddedToCatalogueAvroCodec,
  logger,
}
import shared.infrastructure.KafkaClientsSuite.KafkaEventPublisherSuite
import shared.infrastructure.KafkaTestConfig

import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class KafkaBookInstanceAddedToCatalogueEventPublisherSuite
    extends KafkaEventPublisherSuite[BookInstanceAddedToCatalogue]:
  override def kafkaTestConfig: KafkaTestConfig = KafkaTestConfig.CatalogueBookInstances

  test("should publish new instance books to a topic in kafka") {
    checkUsing(
      bookInstanceAddedToCatalogueGen,
      producer =>
        KafkaBookInstanceAddedToCatalogueEventPublisher(
          producer,
          kafkaTestConfig.kafkaConfig.topic,
          logger,
        ),
    )
  }

object KafkaBookInstanceAddedToCatalogueEventPublisherSuite
    extends BookInstanceAddedToCatalogueAvroCodec:
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
