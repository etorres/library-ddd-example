package es.eriktorr.library
package catalogue.unit

import catalogue.CatalogueConfiguration
import shared.infrastructure.KafkaConfig.{BootstrapServer, ConsumerGroup, SchemaRegistry, Topic}
import shared.infrastructure.{JdbcConfig, KafkaConfig}
import shared.refined.types.NonEmptyString

import cats.data.NonEmptyList
import ciris.Secret
import munit.CatsEffectSuite

import scala.util.Properties

final class CatalogueConfigurationSuite extends CatsEffectSuite:

  /** This test runs only on sbt.
    */
  override def munitIgnore: Boolean = Properties.envOrNone("SBT_TEST_ENV_VARS").isEmpty

  test("load configuration from environment variables") {
    CatalogueConfiguration.load.assertEquals(
      CatalogueConfiguration(
        JdbcConfig(
          NonEmptyString.unsafeFrom("org.postgresql.Driver"),
          NonEmptyString.unsafeFrom("jdbc:postgresql://localhost:5432/test_db_catalogue"),
          NonEmptyString.unsafeFrom("test_jdbc_user_catalogue"),
          Secret(NonEmptyString.unsafeFrom("test_jdbc_password_catalogue")),
        ),
        KafkaConfig(
          NonEmptyList.one(BootstrapServer.unsafeFrom("localhost:29092")),
          ConsumerGroup.unsafeFrom("test_kafka_consumer_group_catalogue"),
          SchemaRegistry.unsafeFrom("http://localhost:8081"),
          Topic.unsafeFrom("test_kafka_topic_book_instances"),
        ),
      ),
    )
  }
