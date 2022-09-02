package es.eriktorr.library
package lending

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

final case class LendingConfiguration(
    jdbcConfig: JdbcConfig,
    bookInstancesKafkaConfig: KafkaConfig,
    bookStateChangesKafkaConfig: KafkaConfig,
    bookStateErrorsKafkaConfig: KafkaConfig,
):
  def asString: String =
    s"""
       |jdbc-lending: ${jdbcConfig.asString}, 
       |kafka-book-instances: ${bookInstancesKafkaConfig.asString}, 
       |kafka-book-state-changes: ${bookStateChangesKafkaConfig.asString}, 
       |kafka-book-state-errors: ${bookStateErrorsKafkaConfig.asString}""".stripMargin
      .replaceAll("\\R", "")
      .nn

object LendingConfiguration extends KafkaConfigConfigDecoder with NonEmptyStringConfigDecoder:
  private[this] val lendingConfiguration = (
    env("JDBC_DRIVER_CLASS_NAME").as[NonEmptyString],
    env("JDBC_CONNECT_URL_LENDING").as[NonEmptyString],
    env("JDBC_USER_LENDING").as[NonEmptyString],
    env("JDBC_PASSWORD_LENDING").as[NonEmptyString].secret,
    env("KAFKA_BOOTSTRAP_SERVERS").as[NonEmptyList[BootstrapServer]],
    env("KAFKA_CONSUMER_GROUP_LENDING").as[ConsumerGroup],
    env("KAFKA_SCHEMA_REGISTRY").as[SchemaRegistry],
    env("KAFKA_TOPIC_BOOK_INSTANCES").as[Topic],
    env("KAFKA_TOPIC_BOOK_STATE_CHANGES").as[Topic],
    env("KAFKA_TOPIC_BOOK_STATE_ERRORS").as[Topic],
  ).parMapN {
    (
        jdbcDriverClassName,
        jdbcConnectUrl,
        jdbcUser,
        jdbcPassword,
        kafkaBootstrapServers,
        kafkaConsumerGroup,
        kafkaSchemaRegistry,
        kafkaTopicBookInstances,
        kafkaTopicBookStateChanges,
        kafkaTopicBookStateErrors,
    ) =>
      LendingConfiguration(
        JdbcConfig(jdbcDriverClassName, jdbcConnectUrl, jdbcUser, jdbcPassword),
        KafkaConfig(
          kafkaBootstrapServers,
          kafkaConsumerGroup,
          kafkaSchemaRegistry,
          kafkaTopicBookInstances,
        ),
        KafkaConfig(
          kafkaBootstrapServers,
          kafkaConsumerGroup,
          kafkaSchemaRegistry,
          kafkaTopicBookStateChanges,
        ),
        KafkaConfig(
          kafkaBootstrapServers,
          kafkaConsumerGroup,
          kafkaSchemaRegistry,
          kafkaTopicBookStateErrors,
        ),
      )
  }

  def load: IO[LendingConfiguration] = lendingConfiguration.load
