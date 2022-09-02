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

final case class CatalogueConfiguration(
    jdbcConfig: JdbcConfig,
    bookInstancesKafkaConfig: KafkaConfig,
):
  def asString: String =
    s"""
       |jdbc-catalogue: ${jdbcConfig.asString}, 
       |kafka-book-instances: ${bookInstancesKafkaConfig.asString}""".stripMargin
      .replaceAll("\\R", "")
      .nn

object CatalogueConfiguration extends KafkaConfigConfigDecoder with NonEmptyStringConfigDecoder:
  private[this] val catalogueConfiguration = (
    env("JDBC_DRIVER_CLASS_NAME").as[NonEmptyString],
    env("JDBC_CONNECT_URL_CATALOGUE").as[NonEmptyString],
    env("JDBC_USER_CATALOGUE").as[NonEmptyString],
    env("JDBC_PASSWORD_CATALOGUE").as[NonEmptyString].secret,
    env("KAFKA_BOOTSTRAP_SERVERS").as[NonEmptyList[BootstrapServer]],
    env("KAFKA_CONSUMER_GROUP_CATALOGUE").as[ConsumerGroup],
    env("KAFKA_SCHEMA_REGISTRY").as[SchemaRegistry],
    env("KAFKA_TOPIC_BOOK_INSTANCES").as[Topic],
  ).parMapN {
    (
        jdbcDriverClassName,
        jdbcConnectUrl,
        jdbcUser,
        jdbcPassword,
        kafkaBootstrapServers,
        kafkaConsumerGroup,
        kafkaSchemaRegistry,
        kafkaTopic,
    ) =>
      CatalogueConfiguration(
        JdbcConfig(jdbcDriverClassName, jdbcConnectUrl, jdbcUser, jdbcPassword),
        KafkaConfig(kafkaBootstrapServers, kafkaConsumerGroup, kafkaSchemaRegistry, kafkaTopic),
      )
  }

  def load: IO[CatalogueConfiguration] = catalogueConfiguration.load
