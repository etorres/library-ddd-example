package es.eriktorr.library
package lending.integration

import book.infrastructure.BookGenerators.bookInstanceAddedToCatalogueGen
import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import lending.infrastructure.KafkaBookInstanceAddedToCatalogueEventHandler
import lending.integration.KafkaBookInstanceAddedToCatalogueEventHandlerSuite.bookInstanceAddedToCatalogueAvroCodec
import shared.infrastructure.FakeEventHandler.EventHandlerState
import shared.infrastructure.{KafkaClientsSuite, KafkaTestConfig}

import cats.effect.{IO, Ref}
import fs2.kafka.{ProducerRecord, ProducerRecords}
import org.scalacheck.effect.PropF.forAllF

import scala.concurrent.duration.*

final class KafkaBookInstanceAddedToCatalogueEventHandlerSuite
    extends KafkaClientsSuite[BookInstanceAddedToCatalogue]:
  override def kafkaTestConfig: KafkaTestConfig = KafkaTestConfig.Lending

  test("should handle events from a kafka topic") {
    forAllF(bookInstanceAddedToCatalogueGen) { event =>
      val (consumer, producer) = kafkaClientsFixture()
      for
        eventHandlerStateRef <- Ref.of[IO, EventHandlerState[BookInstanceAddedToCatalogue]](
          EventHandlerState.empty,
        )
        _ <- producer.produce(
          ProducerRecords.one(
            ProducerRecord(kafkaTestConfig.kafkaConfig.topic.value, event.eventId.value, event),
          ),
        )
        eventHandler = KafkaBookInstanceAddedToCatalogueEventHandler(consumer)
        _ <- eventHandler
          .handleWith(event =>
            eventHandlerStateRef.update(currentState =>
              currentState.copy(event :: currentState.events),
            ),
          )
          .timeout(30.seconds)
          .take(1)
          .compile
          .drain
        finalEventHandlerState <- eventHandlerStateRef.get
      yield assertEquals(finalEventHandlerState.events, List(event))
    }
  }

object KafkaBookInstanceAddedToCatalogueEventHandlerSuite
    extends BookInstanceAddedToCatalogueAvroCodec
