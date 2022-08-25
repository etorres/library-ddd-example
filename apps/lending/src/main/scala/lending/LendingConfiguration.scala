package es.eriktorr.library
package lending

import shared.infrastructure.KafkaConfig.{BootstrapServer, ConsumerGroup, SchemaRegistry, Topic}
import shared.infrastructure.{KafkaConfig, KafkaConfigConfigDecoder}

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.parallel.*
import ciris.env

final case class LendingConfiguration(kafkaConfig: KafkaConfig):
  def asString: String =
    import scala.language.unsafeNulls
    s"""bootstrap-servers=${kafkaConfig.bootstrapServers.toList.mkString(",")}, 
       |consumer-group=${kafkaConfig.consumerGroup}, 
       |topic=${kafkaConfig.topic}, 
       |schema-registry=${kafkaConfig.schemaRegistry}""".stripMargin.replaceAll("\\R", "")

object LendingConfiguration extends KafkaConfigConfigDecoder:
  private[this] val lendingConfiguration = (
    env("KAFKA_BOOTSTRAP_SERVERS").as[NonEmptyList[BootstrapServer]].option,
    env("KAFKA_CONSUMER_GROUP").as[ConsumerGroup].option,
    env("KAFKA_TOPIC").as[Topic].option,
    env("KAFKA_SCHEMA_REGISTRY").as[SchemaRegistry].option,
  ).parMapN {
    (
        kafkaBootstrapServers,
        kafkaConsumerGroup,
        kafkaTopic,
        kafkaSchemaRegistry,
    ) =>
      LendingConfiguration(
        KafkaConfig(
          kafkaBootstrapServers.getOrElse(BootstrapServer.default),
          kafkaConsumerGroup.getOrElse(ConsumerGroup.default),
          kafkaTopic.getOrElse(Topic.default),
          kafkaSchemaRegistry.getOrElse(SchemaRegistry.default),
        ),
      )
  }

  def load: IO[LendingConfiguration] = lendingConfiguration.load
