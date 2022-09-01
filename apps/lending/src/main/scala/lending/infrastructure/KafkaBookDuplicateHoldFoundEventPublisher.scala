package es.eriktorr.library
package lending.infrastructure

import lending.model.BookDuplicateHoldFound
import shared.infrastructure.EventPublisher
import shared.infrastructure.EventPublisher.SimpleEventPublisher
import shared.infrastructure.KafkaClients.KafkaProducerIO
import shared.infrastructure.KafkaConfig.Topic

import cats.effect.IO
import org.typelevel.log4cats.Logger

final class KafkaBookDuplicateHoldFoundEventPublisher(
    producer: KafkaProducerIO[BookDuplicateHoldFound],
    topic: Topic,
    logger: Logger[IO],
) extends SimpleEventPublisher[BookDuplicateHoldFound](producer, topic, logger)
