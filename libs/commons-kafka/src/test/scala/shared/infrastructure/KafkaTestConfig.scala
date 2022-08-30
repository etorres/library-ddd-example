package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaConfig
import shared.infrastructure.KafkaConfig.{BootstrapServer, ConsumerGroup, SchemaRegistry, Topic}
import shared.infrastructure.KafkaTestConfig.{
  testBootstrapServers,
  testConsumerGroupFrom,
  testSchemaRegistry,
  testTopicFrom,
}

import cats.data.NonEmptyList

enum KafkaTestConfig(val kafkaConfig: KafkaConfig):
  case Catalogue
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          KafkaTestConfig.testConsumerGroupFrom("catalogue"),
          KafkaTestConfig.testTopicFrom("catalogue"),
          KafkaTestConfig.testSchemaRegistry,
        ),
      )
  case Lending
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          KafkaTestConfig.testConsumerGroupFrom("lending"),
          KafkaTestConfig.testTopicFrom("lending"),
          KafkaTestConfig.testSchemaRegistry,
        ),
      )

object KafkaTestConfig:
  final private lazy val (testBootstrapServers, testSchemaRegistry) =
    (
      NonEmptyList.one(BootstrapServer.unsafeFrom("localhost:29092")),
      SchemaRegistry.unsafeFrom("http://localhost:8081"),
    )
  final private def testConsumerGroupFrom(name: String) =
    ConsumerGroup.unsafeFrom(s"$name-library-test")
  final private def testTopicFrom(name: String) =
    Topic.unsafeFrom(s"$name-library-test")
