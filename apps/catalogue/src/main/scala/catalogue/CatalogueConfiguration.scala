package es.eriktorr.library
package catalogue

import shared.infrastructure.KafkaConfig.{BootstrapServer, ConsumerGroup, SchemaRegistry, Topic}
import shared.infrastructure.{
  JdbcConfig,
  KafkaConfig,
  KafkaConfigConfigDecoder,
  NonEmptyStringConfigDecoder,
}
import shared.refined.types.NonEmptyString

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.parallel.*
import ciris.env

final case class CatalogueConfiguration(jdbcConfig: JdbcConfig, kafkaConfig: KafkaConfig):
  def asString: String = s"${jdbcConfig.asString}, ${kafkaConfig.asString}"

object CatalogueConfiguration extends KafkaConfigConfigDecoder with NonEmptyStringConfigDecoder:
  private[this] val catalogueConfiguration = (
    env("JDBC_DRIVER_CLASS_NAME").as[NonEmptyString],
    env("JDBC_CONNECT_URL").as[NonEmptyString],
    env("JDBC_USER").as[NonEmptyString],
    env("JDBC_PASSWORD").as[NonEmptyString].secret,
    env("KAFKA_BOOTSTRAP_SERVERS").as[NonEmptyList[BootstrapServer]],
    env("KAFKA_CONSUMER_GROUP").as[ConsumerGroup],
    env("KAFKA_TOPIC").as[Topic],
    env("KAFKA_SCHEMA_REGISTRY").as[SchemaRegistry],
  ).parMapN {
    (
        jdbcDriverClassName,
        jdbcConnectUrl,
        jdbcUser,
        jdbcPassword,
        kafkaBootstrapServers,
        kafkaConsumerGroup,
        kafkaTopic,
        kafkaSchemaRegistry,
    ) =>
      CatalogueConfiguration(
        JdbcConfig(jdbcDriverClassName, jdbcConnectUrl, jdbcUser, jdbcPassword),
        KafkaConfig(kafkaBootstrapServers, kafkaConsumerGroup, kafkaTopic, kafkaSchemaRegistry),
      )
  }

  def load: IO[CatalogueConfiguration] = catalogueConfiguration.load
