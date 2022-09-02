package es.eriktorr.library
package lending.integration

import lending.infrastructure.LendingGenerators.bookStateChangedGen
import lending.infrastructure.{BookStateChangedAvroCodec, KafkaBookStateChangedEvenHandler}
import lending.integration.KafkaBookStateChangedEvenHandlerSuite.{bookStateChangedAvroCodec, logger}
import lending.model.BookStateChanged
import shared.infrastructure.FakeEventHandler.EventHandlerState
import shared.infrastructure.{KafkaClientsSuite, KafkaTestConfig}

import cats.effect.{IO, Ref}
import fs2.kafka.{ProducerRecord, ProducerRecords}
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.*

final class KafkaBookStateChangedEvenHandlerSuite extends KafkaClientsSuite[BookStateChanged]:
  override def kafkaTestConfig: KafkaTestConfig = KafkaTestConfig.LendingBookStateChanges

  test("should handle book state changed event in a kafka topic") {
    forAllF(bookStateChangedGen) { event =>
      val (consumer, producer) = kafkaClientsFixture()
      for
        stateRef <- Ref.of[IO, EventHandlerState[BookStateChanged]](
          EventHandlerState.empty,
        )
        _ <- producer.produce(
          ProducerRecords.one(
            ProducerRecord(kafkaTestConfig.kafkaConfig.topic.value, event.eventId.asString, event),
          ),
        )
        eventHandler = KafkaBookStateChangedEvenHandler(consumer)
        _ <- eventHandler
          .handleWith(event =>
            stateRef.update(currentState => currentState.copy(event :: currentState.events)),
          )
          .timeout(30.seconds)
          .take(1)
          .compile
          .drain
        finalState <- stateRef.get
      yield assertEquals(finalState.events, List(event))
    }
  }

object KafkaBookStateChangedEvenHandlerSuite extends BookStateChangedAvroCodec:
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
