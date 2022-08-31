package es.eriktorr.library
package book.infrastructure

import book.model.BookId

import cats.syntax.either.*
import vulcan.{AvroError, Codec}

trait BookIdAvroCodec:
  implicit val bookIdAvroCodec: Codec[BookId] = Codec.uuid.imap(BookId.from)(_.value)
