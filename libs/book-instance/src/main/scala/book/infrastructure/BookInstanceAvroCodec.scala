package es.eriktorr.library
package book.infrastructure

import book.model.BookInstance
import shared.Namespaces
import shared.infrastructure.EventIdAvroCodec

import cats.syntax.apply.*
import vulcan.Codec

trait BookInstanceAvroCodec extends BookTypeAvroCodec with ISBNAvroCodec with BookIdAvroCodec:
  implicit val bookInstanceAvroCodec: Codec[BookInstance] =
    Codec.record(
      name = "BookInstance",
      namespace = Namespaces.default,
      doc = Some("A book instance"),
    ) { field =>
      (
        field("bookId", _.bookId),
        field("isbn", _.isbn),
        field("bookType", _.bookType),
      ).mapN(BookInstance.apply)
    }
