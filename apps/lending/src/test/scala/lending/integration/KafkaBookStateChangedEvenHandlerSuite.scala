package es.eriktorr.library
package lending.integration

import lending.infrastructure.LendingGenerators.bookStateChangedGen
import lending.infrastructure.{BookStateChangedAvroCodec, KafkaBookStateChangedEvenHandler}
import lending.integration.KafkaBookStateChangedEvenHandlerSuite.{bookStateChangedAvroCodec, logger}
import lending.model.BookStateChanged
import shared.infrastructure.KafkaClientsSuite.KafkaEventHandlerSuite
import shared.infrastructure.KafkaTestConfig

import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class KafkaBookStateChangedEvenHandlerSuite extends KafkaEventHandlerSuite[BookStateChanged]:
  override def kafkaTestConfig: KafkaTestConfig = KafkaTestConfig.LendingBookStateChanges

  test("should handle book state changed event in a kafka topic") {
    checkUsing(bookStateChangedGen, KafkaBookStateChangedEvenHandler.apply(_))
  }

object KafkaBookStateChangedEvenHandlerSuite extends BookStateChangedAvroCodec:
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
