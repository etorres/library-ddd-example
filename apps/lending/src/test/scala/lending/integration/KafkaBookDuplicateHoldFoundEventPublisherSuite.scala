package es.eriktorr.library
package lending.integration

import lending.infrastructure.LendingGenerators.bookDuplicateHoldFoundGen
import lending.infrastructure.{
  BookDuplicateHoldFoundAvroCodec,
  KafkaBookDuplicateHoldFoundEventPublisher,
}
import lending.integration.KafkaBookDuplicateHoldFoundEventPublisherSuite.{
  bookDuplicateHoldFoundAvroCodec,
  logger,
}
import lending.model.BookDuplicateHoldFound
import shared.infrastructure.KafkaClientsSuite.KafkaEventPublisherSuite
import shared.infrastructure.KafkaTestConfig

import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class KafkaBookDuplicateHoldFoundEventPublisherSuite
    extends KafkaEventPublisherSuite[BookDuplicateHoldFound]:
  override def kafkaTestConfig: KafkaTestConfig = KafkaTestConfig.LendingBookStateErrors

  test("should publish book state errors to a topic in kafka") {
    checkUsing(
      bookDuplicateHoldFoundGen,
      producer =>
        KafkaBookDuplicateHoldFoundEventPublisher(
          producer,
          kafkaTestConfig.kafkaConfig.topic,
          logger,
        ),
    )
  }

object KafkaBookDuplicateHoldFoundEventPublisherSuite extends BookDuplicateHoldFoundAvroCodec:
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
