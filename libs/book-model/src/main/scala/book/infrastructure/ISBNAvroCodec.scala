package es.eriktorr.library
package book.infrastructure

import book.model.ISBN

import cats.syntax.either.*
import vulcan.{AvroError, Codec}

trait ISBNAvroCodec:
  implicit val isbnAvroCodec: Codec[ISBN] =
    Codec.string.imapError(
      ISBN.from(_).toEither.leftMap(error => AvroError(ValidationErrors(error).getMessage)),
    )(_.value)
