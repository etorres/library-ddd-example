package es.eriktorr.library
package lending.infrastructure

import book.model.BookId
import shared.infrastructure.StringFieldJsonDecoder
import shared.validated.ValidatedEither.validatedNecEither

import io.circe.{Decoder, Encoder, Json}

trait BookIdJsonCodec extends StringFieldJsonDecoder:
  implicit val bookIdJsonDecoder: Decoder[BookId] = decodeValue[BookId](BookId.from(_).either)

  implicit val bookIdJsonEncoder: Encoder[BookId] = (bookId: BookId) =>
    Json.fromString(bookId.asString)
