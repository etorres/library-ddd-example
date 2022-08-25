package es.eriktorr.library
package shared.refined.types.infrastructure

import shared.ValidationErrors
import shared.refined.types.UUID

import cats.syntax.either.*
import vulcan.{AvroError, Codec}

trait UUIDAvroCodec:
  implicit val uuidAvroCodec: Codec[UUID] =
    Codec.string.imapError(
      UUID.from(_).toEither.leftMap(error => AvroError(ValidationErrors(error).getMessage)),
    )(_.value)
