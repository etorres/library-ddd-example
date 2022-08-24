package es.eriktorr.library
package lending.infrastructure

import book.model.BookInstance

import cats.syntax.all.*
import vulcan.Codec

trait BookInstanceAvroCodec extends BookTypeAvroCodec with ISBNAvroCodec with UUIDAvroCodec:
  implicit val bookInstanceAvroCodec: Codec[BookInstance] =
    Codec.record(
      name = "BookInstance",
      namespace = AvroNamespaces.default,
      doc = Some("A book instance"),
    ) { field =>
      (
        field("bookId", _.bookId),
        field("isbn", _.isbn),
        field("bookType", _.bookType),
      ).mapN(BookInstance.apply)
    }
