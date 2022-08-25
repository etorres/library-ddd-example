package es.eriktorr.library
package shared.infrastructure

import _root_.vulcan.Codec
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import fs2.kafka.*
import fs2.kafka.vulcan.{
  avroDeserializer,
  avroSerializer,
  AvroSettings,
  SchemaRegistryClientSettings,
}

object KafkaClients:
  type KafkaConsumerIO[A] = KafkaConsumer[IO, String, A]
  type KafkaProducerIO[A] = KafkaProducer[IO, String, A]

  def defaultKafkaClients[A](using
      coderDecoder: Codec[A],
  ): Resource[IO, (KafkaConsumerIO[A], KafkaProducerIO[A])] =
    val kafkaConfig = KafkaConfig.default
    Resource
      .eval[IO, (Resource[IO, KafkaConsumerIO[A]], Resource[IO, KafkaProducerIO[A]])] {
        avroSettingsFrom[A](kafkaConfig).map { avroSettings =>
          val consumer = consumerFrom[A](kafkaConfig, avroSettings)
          val producer = producerFrom[A](kafkaConfig, avroSettings)
          consumer -> producer
        }
      }
      .flatMap { (consumer, producer) =>
        (consumer, producer).tupled
      }

  def kafkaConsumerUsing[A](
      kafkaConfig: KafkaConfig,
  )(using coderDecoder: Codec[A]): Resource[IO, KafkaConsumerIO[A]] =
    Resource
      .eval[IO, AvroSettings[IO]](avroSettingsFrom(kafkaConfig))
      .flatMap(consumerFrom(kafkaConfig, _))

  def kafkaProducerUsing[A](
      kafkaConfig: KafkaConfig,
  )(using coderDecoder: Codec[A]): Resource[IO, KafkaProducerIO[A]] =
    Resource
      .eval[IO, AvroSettings[IO]](avroSettingsFrom(kafkaConfig))
      .flatMap(producerFrom(kafkaConfig, _))

  private[this] def avroSettingsFrom[A](kafkaConfig: KafkaConfig)(using
      coderDecoder: Codec[A],
  ): IO[AvroSettings[IO]] =
    val avroSettingsSharedClient: IO[AvroSettings[IO]] = SchemaRegistryClientSettings[IO](
      kafkaConfig.schemaRegistry.value,
    ).createSchemaRegistryClient.map(AvroSettings(_))

    avroSettingsSharedClient
      .flatMap { avroSettings =>
        val avroSettingsWithoutAutoRegister =
          avroSettings
            .withAutoRegisterSchemas(false)
            .withProperties(
              "auto.register.schemas" -> "false",
              "use.latest.version" -> "true",
            )

        avroSettingsWithoutAutoRegister.registerSchema[String](
          s"${kafkaConfig.topic}-key",
        ) *>
          avroSettingsWithoutAutoRegister.registerSchema[A](
            s"${kafkaConfig.topic}-value",
          ) *> IO(avroSettingsWithoutAutoRegister)
      }

  private[this] def consumerFrom[A](
      kafkaConfig: KafkaConfig,
      avroSettings: AvroSettings[IO],
  )(using coderDecoder: Codec[A]): Resource[IO, KafkaConsumerIO[A]] =
    implicit val eventDeserializer: RecordDeserializer[IO, A] =
      avroDeserializer[A].using(avroSettings)

    val consumerSettings = ConsumerSettings[IO, String, A]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(kafkaConfig.bootstrapServersAsString)
      .withGroupId(kafkaConfig.consumerGroup.value)

    KafkaConsumer
      .resource(consumerSettings)
      .evalTap(_.subscribeTo(kafkaConfig.topic.value))

  private[this] def producerFrom[A](
      kafkaConfig: KafkaConfig,
      avroSettings: AvroSettings[IO],
  )(using coderDecoder: Codec[A]): Resource[IO, KafkaProducerIO[A]] =
    implicit val eventSerializer: RecordSerializer[IO, A] =
      avroSerializer[A].using(avroSettings)

    val producerSettings = ProducerSettings[IO, String, A]
      .withBootstrapServers(kafkaConfig.bootstrapServersAsString)

    KafkaProducer.resource(producerSettings)
