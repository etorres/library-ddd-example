package es.eriktorr.library
package lending.infrastructure

import book.model.BookInstanceAddedToCatalogue
import lending.model.BookInstanceAddedToCatalogueEventHandler
import shared.infrastructure.KafkaClients.KafkaConsumerIO

import cats.effect.IO
import fs2.Stream
import fs2.kafka.commitBatchWithin

import scala.concurrent.duration.*

final class KafkaBookInstanceAddedToCatalogueEventHandler(
    consumer: KafkaConsumerIO[BookInstanceAddedToCatalogue],
) extends BookInstanceAddedToCatalogueEventHandler:
  override def handleWith(f: BookInstanceAddedToCatalogue => IO[Unit]): Stream[IO, Unit] =
    consumer.stream
      .mapAsync(16) { committable =>
        val event = committable.record.value
        f.apply(event).as(committable.offset)
      }
      .through(commitBatchWithin(100, 15.seconds))
