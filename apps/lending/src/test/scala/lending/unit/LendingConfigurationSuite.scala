package es.eriktorr.library
package lending.unit

import lending.LendingConfiguration
import shared.infrastructure.KafkaConfig.{BootstrapServer, ConsumerGroup, SchemaRegistry, Topic}
import shared.infrastructure.{JdbcConfig, KafkaConfig}
import shared.refined.types.NonEmptyString

import cats.data.NonEmptyList
import ciris.Secret
import munit.CatsEffectSuite

import scala.util.Properties

final class LendingConfigurationSuite extends CatsEffectSuite:

  /** This test runs only on sbt.
    */
  override def munitIgnore: Boolean = Properties.envOrNone("SBT_TEST_ENV_VARS").isEmpty

  test("load configuration from environment variables") {
    LendingConfiguration.load.assertEquals(
      LendingConfiguration(
        JdbcConfig(
          NonEmptyString.unsafeFrom("org.postgresql.Driver"),
          NonEmptyString.unsafeFrom("jdbc:postgresql://localhost:5432/test_db_lending"),
          NonEmptyString.unsafeFrom("test_jdbc_user_lending"),
          Secret(NonEmptyString.unsafeFrom("test_jdbc_password_lending")),
        ),
        KafkaConfig(
          NonEmptyList.one(BootstrapServer.unsafeFrom("localhost:29092")),
          ConsumerGroup.unsafeFrom("test_kafka_consumer_group_lending"),
          SchemaRegistry.unsafeFrom("http://localhost:8081"),
          Topic.unsafeFrom("test_kafka_topic_book_instances"),
        ),
        KafkaConfig(
          NonEmptyList.one(BootstrapServer.unsafeFrom("localhost:29092")),
          ConsumerGroup.unsafeFrom("test_kafka_consumer_group_lending"),
          SchemaRegistry.unsafeFrom("http://localhost:8081"),
          Topic.unsafeFrom("test_kafka_topic_book_state_changes"),
        ),
        KafkaConfig(
          NonEmptyList.one(BootstrapServer.unsafeFrom("localhost:29092")),
          ConsumerGroup.unsafeFrom("test_kafka_consumer_group_lending"),
          SchemaRegistry.unsafeFrom("http://localhost:8081"),
          Topic.unsafeFrom("test_kafka_topic_book_state_errors"),
        ),
      ),
    )
  }
