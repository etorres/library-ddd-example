package es.eriktorr.library
package catalogue.infrastructure

import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.EventPublisher
import shared.infrastructure.KafkaClients.KafkaProducerIO

import cats.effect.IO
import fs2.kafka.{ProducerRecord, ProducerRecords}
import org.typelevel.log4cats.Logger

final class KafkaBookInstanceAddedToCatalogueEventPublisher(
    producer: KafkaProducerIO[BookInstanceAddedToCatalogue],
    topic: String,
    logger: Logger[IO],
) extends EventPublisher[BookInstanceAddedToCatalogue]:
  override def publish(event: BookInstanceAddedToCatalogue): IO[Unit] = IO.unit <* producer
    .produce(ProducerRecords.one(ProducerRecord(topic, event.eventId.value, event)))
    .handleErrorWith { case error: Throwable =>
      logger.error(error)("The event could not be published") *> IO.raiseError(error)
    }
