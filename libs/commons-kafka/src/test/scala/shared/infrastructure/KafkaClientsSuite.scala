package es.eriktorr.library
package shared.infrastructure

import shared.DomainEvent
import shared.infrastructure.FakeEventHandler.EventHandlerState
import shared.infrastructure.FakeEventPublisher.EventPublisherState
import shared.infrastructure.KafkaClients.{KafkaConsumerIO, KafkaProducerIO}

import cats.effect.{IO, Ref}
import fs2.kafka.{commitBatchWithin, ProducerRecord, ProducerRecords}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF
import org.scalacheck.effect.PropF.forAllF
import org.scalacheck.{Gen, Test}
import org.typelevel.log4cats.Logger
import vulcan.Codec

import scala.concurrent.duration.*

abstract class KafkaClientsSuite[A <: DomainEvent](using coderDecoder: Codec[A], logger: Logger[IO])
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:
  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(1).withWorkers(1)

  def kafkaTestConfig: KafkaTestConfig

  val kafkaClientsFixture: Fixture[(KafkaConsumerIO[A], KafkaProducerIO[A])] =
    ResourceSuiteLocalFixture(
      "kafka-clients",
      KafkaTestClients.kafkaTestClientsUsing[A](kafkaTestConfig),
    )

  override def munitFixtures: Seq[Fixture[?]] = List(kafkaClientsFixture)

object KafkaClientsSuite:
  abstract class KafkaEventHandlerSuite[A <: DomainEvent](using
      coderDecoder: Codec[A],
      logger: Logger[IO],
  ) extends KafkaClientsSuite[A]:
    def checkUsing(
        genA: Gen[A],
        eventHandlerProvider: KafkaConsumerIO[A] => EventHandler[A],
    ): PropF[IO] =
      forAllF(genA) { event =>
        val (consumer, producer) = kafkaClientsFixture()
        for
          stateRef <- Ref.of[IO, EventHandlerState[A]](
            EventHandlerState.empty,
          )
          _ <- producer.produce(
            ProducerRecords.one(
              ProducerRecord(kafkaTestConfig.kafkaConfig.topic.value, event.eventId.asString, event),
            ),
          )
          eventHandler = eventHandlerProvider.apply(consumer)
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

  abstract class KafkaEventPublisherSuite[A <: DomainEvent](using
      coderDecoder: Codec[A],
      logger: Logger[IO],
  ) extends KafkaClientsSuite[A]:
    def checkUsing(
        genA: Gen[A],
        eventPublisherProvider: KafkaProducerIO[A] => EventPublisher[A],
    ): PropF[IO] =
      forAllF(genA) { event =>
        val (consumer, producer) = kafkaClientsFixture()
        for
          stateRef <- Ref.of[IO, EventPublisherState[A]](
            EventPublisherState.empty,
          )
          eventPublisher = eventPublisherProvider.apply(producer)
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
