package es.eriktorr.library
package shared.infrastructure

import shared.concurrent.OnErrorRetry
import shared.concurrent.OnErrorRetry.{RetryConfig, RetryOutcome}
import shared.infrastructure.KafkaClients.{KafkaConsumerIO, KafkaProducerIO}

import cats.effect.{IO, Resource}
import fs2.kafka.{AdminClientSettings, KafkaAdminClient}
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import org.typelevel.log4cats.Logger
import vulcan.Codec

import scala.concurrent.duration.*

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
      val retryConfig = RetryConfig(
        maxRetries = 10,
        initialDelay = 10.millis,
        maxDelay = 2.seconds,
        backoffFactor = 1.5,
      )

      lazy val topic: String = kafkaTestConfig.kafkaConfig.topic.value

      def topicExists() = kafkaAdminClient.listTopics.names.map(_.contains(topic))
      def deleteTopic() =
        kafkaAdminClient.deleteTopic(topic) *> logger.debug(s"Topic deleted: $topic")
      def createTopic() = OnErrorRetry.withBackoff(
        kafkaAdminClient.createTopic(new NewTopic(topic, 1, 1.toShort)) *> logger.debug(
          s"Topic created: $topic",
        ),
        retryConfig,
      ) {
        case _: TopicExistsException => IO.pure(RetryOutcome.Next)
        case e => logger.error(e)("Unexpected error, giving up").as(RetryOutcome.Raise)
      }

      topicExists().ifM(ifTrue = deleteTopic() *> createTopic(), ifFalse = IO.unit)
    }(_ => IO.unit)
    kafkaClients <- KafkaClients.kafkaClientsUsing[A](kafkaTestConfig.kafkaConfig)
  yield kafkaClients
