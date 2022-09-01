package es.eriktorr.library
package lending.infrastructure

import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.EventHandler
import shared.infrastructure.EventHandler.CommittableEventHandler
import shared.infrastructure.KafkaClients.KafkaConsumerIO

final class KafkaBookInstanceAddedToCatalogueEventHandler(
    consumer: KafkaConsumerIO[BookInstanceAddedToCatalogue],
) extends CommittableEventHandler[BookInstanceAddedToCatalogue](consumer)
