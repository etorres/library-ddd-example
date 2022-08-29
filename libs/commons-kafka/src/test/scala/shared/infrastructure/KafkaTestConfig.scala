package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaConfig.BootstrapServer
import shared.infrastructure.KafkaConfig.ConsumerGroup
import shared.infrastructure.KafkaConfig.SchemaRegistry
import shared.infrastructure.KafkaConfig.Topic
import shared.infrastructure.KafkaConfig
import shared.infrastructure.KafkaTestConfig.{testBootstrapServers, testSchemaRegistry, testTopic}

import cats.data.NonEmptyList

enum KafkaTestConfig(val kafkaConfig: KafkaConfig):
  case Catalogue
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          ConsumerGroup.unsafeFrom("library"),
          KafkaTestConfig.testTopic,
          KafkaTestConfig.testSchemaRegistry,
        ),
      )
  case Lending
      extends KafkaTestConfig(
        KafkaConfig(
          KafkaTestConfig.testBootstrapServers,
          ConsumerGroup.unsafeFrom("library"),
          KafkaTestConfig.testTopic,
          KafkaTestConfig.testSchemaRegistry,
        ),
      )

object KafkaTestConfig:
  final private lazy val (testBootstrapServers, testSchemaRegistry, testTopic) = (
    NonEmptyList.one(BootstrapServer.unsafeFrom("localhost:29092")),
    SchemaRegistry.unsafeFrom("http://localhost:8081"),
    Topic.unsafeFrom("library-tests"),
  )
