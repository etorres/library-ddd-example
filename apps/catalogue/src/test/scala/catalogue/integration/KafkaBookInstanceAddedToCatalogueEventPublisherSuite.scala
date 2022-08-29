package es.eriktorr.library
package catalogue.integration

import book.infrastructure.BookGenerators.bookInstanceAddedToCatalogueGen
import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import catalogue.infrastructure.KafkaBookInstanceAddedToCatalogueEventPublisher
import catalogue.integration.KafkaBookInstanceAddedToCatalogueEventPublisherSuite.bookInstanceAddedToCatalogueAvroCodec
import shared.infrastructure.FakeEventPublisher.EventPublisherState
import shared.infrastructure.{KafkaClientsSuite, KafkaTestConfig}

import cats.effect.{IO, Ref}
import fs2.kafka.commitBatchWithin
import org.scalacheck.effect.PropF.forAllF
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.*

final class KafkaBookInstanceAddedToCatalogueEventPublisherSuite
    extends KafkaClientsSuite[BookInstanceAddedToCatalogue]:
  override def kafkaTestConfig: KafkaTestConfig = KafkaTestConfig.Catalogue

  test("should publish events to a topic in kafka") {
    forAllF(bookInstanceAddedToCatalogueGen) { event =>
      val (consumer, producer) = kafkaClientsFixture()
      for
        stateRef <- Ref.of[IO, EventPublisherState[BookInstanceAddedToCatalogue]](
          EventPublisherState.empty,
        )
        logger <- Slf4jLogger.create[IO]
        eventPublisher = KafkaBookInstanceAddedToCatalogueEventPublisher(
          producer,
          kafkaTestConfig.kafkaConfig.topic.value,
          logger,
        )
        _ <- eventPublisher.publish(event)
        _ <- consumer.stream
          .evalMap { committable =>
            stateRef
              .update(currentState =>
                currentState.copy(committable.record.value :: currentState.events),
              )
              .as(committable.offset)
          }
          .through(commitBatchWithin(100, 15.seconds))
          .timeout(30.seconds)
          .take(1)
          .compile
          .drain
        finalState <- stateRef.get
      yield assertEquals(finalState.events, List(event))
    }
  }

object KafkaBookInstanceAddedToCatalogueEventPublisherSuite
    extends BookInstanceAddedToCatalogueAvroCodec
