package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaClients.{KafkaConsumerIO, KafkaProducerIO}

import cats.effect.{IO, Resource}
import fs2.kafka.{AdminClientSettings, KafkaAdminClient}
import org.apache.kafka.clients.admin.NewTopic
import org.typelevel.log4cats.Logger
import vulcan.Codec

object KafkaTestClients:
  def kafkaTestClientsUsing[A](
      kafkaTestConfig: KafkaTestConfig,
  )(using
      coderDecoder: Codec[A],
      logger: Logger[IO],
  ): Resource[IO, (KafkaConsumerIO[A], KafkaProducerIO[A])] = for
    kafkaAdminClient <- KafkaAdminClient
      .resource[IO](
        AdminClientSettings(kafkaTestConfig.kafkaConfig.bootstrapServersAsString),
      )
    _ <- Resource.make {
      for
        topicNames <- kafkaAdminClient.listTopics.names
        _ <- logger.debug(s"Kafka topics: ${topicNames.mkString(", ")}")
//        _ <- kafkaAdminClient.deleteTopic(kafkaTestConfig.kafkaConfig.topic.value)
//        // TODO: polling until deleted
//        _ <- kafkaAdminClient.createTopic(
//          NewTopic(kafkaTestConfig.kafkaConfig.topic.value, 1, 1.toShort),
//        )
//        // TODO: polling until created
      yield ()
    }(_ => IO.unit)
    kafkaClients <- KafkaClients.kafkaClientsUsing[A](kafkaTestConfig.kafkaConfig)
  yield kafkaClients
