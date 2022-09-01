package es.eriktorr.library
package lending.infrastructure

import lending.model.BookStateChange.*
import shared.infrastructure.EventHandler.CommittableEventHandler
import shared.infrastructure.KafkaClients.KafkaConsumerIO

object KafkaBookStateChangedEvenHandlers:
  final class KakfaBookPlacedOnHoldEventHandler(consumer: KafkaConsumerIO[BookPlacedOnHold])
      extends CommittableEventHandler[BookPlacedOnHold](consumer)

  final class KakfaBookCheckedOutEventHandler(consumer: KafkaConsumerIO[BookCheckedOut])
      extends CommittableEventHandler[BookCheckedOut](consumer)

  final class KakfaBookHoldExpiredEventHandler(consumer: KafkaConsumerIO[BookHoldExpired])
      extends CommittableEventHandler[BookHoldExpired](consumer)

  final class KakfaBookHoldCanceledEventHandler(consumer: KafkaConsumerIO[BookHoldCanceled])
      extends CommittableEventHandler[BookHoldCanceled](consumer)

  final class KakfaBookReturnedEventHandler(consumer: KafkaConsumerIO[BookReturned])
      extends CommittableEventHandler[BookReturned](consumer)
