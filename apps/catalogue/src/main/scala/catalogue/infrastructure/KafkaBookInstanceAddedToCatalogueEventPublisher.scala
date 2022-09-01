package es.eriktorr.library
package catalogue.infrastructure

import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.EventPublisher.SimpleEventPublisher
import shared.infrastructure.KafkaClients.KafkaProducerIO
import shared.infrastructure.KafkaConfig.Topic

import cats.effect.IO
import org.typelevel.log4cats.Logger

final class KafkaBookInstanceAddedToCatalogueEventPublisher(
    producer: KafkaProducerIO[BookInstanceAddedToCatalogue],
    topic: Topic,
    logger: Logger[IO],
) extends SimpleEventPublisher[BookInstanceAddedToCatalogue](producer, topic, logger)
