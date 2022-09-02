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
  case CatalogueBookInstances
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          KafkaTestConfig.testConsumerGroupFrom("catalogue"),
          KafkaTestConfig.testSchemaRegistry,
          KafkaTestConfig.testTopicFrom("book-instances-catalogue"),
        ),
      )
  case LendingBookInstances
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          KafkaTestConfig.testConsumerGroupFrom("lending"),
          KafkaTestConfig.testSchemaRegistry,
          KafkaTestConfig.testTopicFrom("book-instances-lending"),
        ),
      )
  case LendingBookStateChanges
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          KafkaTestConfig.testConsumerGroupFrom("lending"),
          KafkaTestConfig.testSchemaRegistry,
          KafkaTestConfig.testTopicFrom("book-state-changes-lending"),
        ),
      )
  case LendingBookStateErrors
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          KafkaTestConfig.testConsumerGroupFrom("lending"),
          KafkaTestConfig.testSchemaRegistry,
          KafkaTestConfig.testTopicFrom("book-state-errors-lending"),
        ),
      )

object KafkaTestConfig:
  final private lazy val (testBootstrapServers, testSchemaRegistry) =
    (
      NonEmptyList.one(BootstrapServer.unsafeFrom("localhost:29092")),
      SchemaRegistry.unsafeFrom("http://localhost:8081"),
    )
  final private def testConsumerGroupFrom(name: String) =
    ConsumerGroup.unsafeFrom(s"$name-test")
  final private def testTopicFrom(name: String) =
    Topic.unsafeFrom(s"$name-test")
