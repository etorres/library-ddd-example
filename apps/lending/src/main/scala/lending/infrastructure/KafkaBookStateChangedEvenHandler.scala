package es.eriktorr.library
package lending.infrastructure

import lending.model.BookStateChanged
import shared.infrastructure.EventHandler.CommittableEventHandler
import shared.infrastructure.KafkaClients.KafkaConsumerIO

final class KafkaBookStateChangedEvenHandler(consumer: KafkaConsumerIO[BookStateChanged])
    extends CommittableEventHandler[BookStateChanged](consumer)
