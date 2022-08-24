package es.eriktorr.library
package lending.infrastructure

import refined.types.UUID

import cats.syntax.either.*
import vulcan.{AvroError, Codec}

trait UUIDAvroCodec:
  implicit val uuidAvroCodec: Codec[UUID] =
    Codec.string.imapError(
      UUID.from(_).toEither.leftMap(error => AvroError(ValidationErrors(error).getMessage)),
    )(_.value)
