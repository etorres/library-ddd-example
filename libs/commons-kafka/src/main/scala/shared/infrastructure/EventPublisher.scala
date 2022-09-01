package es.eriktorr.library
package shared.infrastructure

import shared.DomainEvent
import shared.infrastructure.KafkaClients.KafkaProducerIO
import shared.infrastructure.KafkaConfig.Topic

import cats.effect.IO
import fs2.kafka.{ProducerRecord, ProducerRecords}
import org.typelevel.log4cats.Logger

trait EventPublisher[A <: DomainEvent]:
  def publish(event: A): IO[Unit]

object EventPublisher:
  abstract class SimpleEventPublisher[A <: DomainEvent](
      producer: KafkaProducerIO[A],
      topic: Topic,
      logger: Logger[IO],
  ) extends EventPublisher[A]:
    override def publish(event: A): IO[Unit] = IO.unit <* producer
      .produce(ProducerRecords.one(ProducerRecord(topic.value, event.eventId.asString, event)))
      .handleErrorWith { case error: Throwable =>
        logger.error(error)("The event could not be published") *> IO.raiseError(error)
      }
