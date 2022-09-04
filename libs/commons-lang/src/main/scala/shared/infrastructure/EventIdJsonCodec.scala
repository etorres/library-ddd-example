package es.eriktorr.library
package shared.infrastructure

import shared.EventId
import shared.validated.ValidatedEither.validatedNecEither

import io.circe.{Decoder, Encoder, Json}

trait EventIdJsonCodec extends StringFieldJsonDecoder:
  implicit val eventIdJsonDecoder: Decoder[EventId] = decodeValue[EventId](EventId.from(_).either)

  implicit val eventIdJsonEncoder: Encoder[EventId] = (eventId: EventId) =>
    Json.fromString(eventId.asString)
