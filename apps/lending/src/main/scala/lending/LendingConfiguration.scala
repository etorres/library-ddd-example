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

final case class LendingConfiguration(jdbcConfig: JdbcConfig, kafkaConfig: KafkaConfig):
  def asString: String =
    import scala.language.unsafeNulls
    s"""bootstrap-servers=${kafkaConfig.bootstrapServers.toList.mkString(",")}, 
       |consumer-group=${kafkaConfig.consumerGroup}, 
       |topic=${kafkaConfig.topic}, 
       |schema-registry=${kafkaConfig.schemaRegistry}""".stripMargin.replaceAll("\\R", "")

object LendingConfiguration extends KafkaConfigConfigDecoder with NonEmptyStringConfigDecoder:
  private[this] val lendingConfiguration = (
    env("JDBC_DRIVER_CLASS_NAME").as[NonEmptyString].option,
    env("JDBC_CONNECT_URL").as[NonEmptyString].option,
    env("JDBC_USER").as[NonEmptyString].option,
    env("JDBC_PASSWORD").as[NonEmptyString].secret,
    env("KAFKA_BOOTSTRAP_SERVERS").as[NonEmptyList[BootstrapServer]].option,
    env("KAFKA_CONSUMER_GROUP").as[ConsumerGroup].option,
    env("KAFKA_TOPIC").as[Topic].option,
    env("KAFKA_SCHEMA_REGISTRY").as[SchemaRegistry].option,
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
      LendingConfiguration(
        JdbcConfig(
          jdbcDriverClassName.getOrElse(JdbcConfig.default.driverClassName),
          jdbcConnectUrl.getOrElse(JdbcConfig.default.connectUrl),
          jdbcUser.getOrElse(JdbcConfig.default.user),
          jdbcPassword,
        ),
        KafkaConfig(
          kafkaBootstrapServers.getOrElse(BootstrapServer.default),
          kafkaConsumerGroup.getOrElse(ConsumerGroup.default),
          kafkaTopic.getOrElse(Topic.default),
          kafkaSchemaRegistry.getOrElse(SchemaRegistry.default),
        ),
      )
  }

  def load: IO[LendingConfiguration] = lendingConfiguration.load
