package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaClients
import shared.infrastructure.KafkaClients.{KafkaConsumerIO, KafkaProducerIO}

import cats.effect.IO
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Test
import vulcan.Codec

abstract class KafkaClientsSuite[A](using coderDecoder: Codec[A])
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:
  override def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters.withMinSuccessfulTests(1).withWorkers(1)

  val kafkaClientsFixture: Fixture[(KafkaConsumerIO[A], KafkaProducerIO[A])] =
    ResourceSuiteLocalFixture("kafka-clients", KafkaClients.defaultKafkaClients)

  override def munitFixtures: Seq[Fixture[?]] = List(kafkaClientsFixture)
