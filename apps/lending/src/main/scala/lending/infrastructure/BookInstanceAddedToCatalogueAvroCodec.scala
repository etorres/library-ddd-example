package es.eriktorr.library
package lending.infrastructure

import lending.model.BookInstanceAddedToCatalogue

import cats.syntax.all.*
import vulcan.{AvroError, Codec}

trait BookInstanceAddedToCatalogueAvroCodec extends BookInstanceAvroCodec with UUIDAvroCodec:
  implicit val bookInstanceAddedToCatalogueAvroCodec: Codec[BookInstanceAddedToCatalogue] =
    Codec.record(
      name = "BookInstanceAddedToCatalogue",
      namespace = AvroNamespaces.default,
      doc = Some("An event emitted everytime a new book instance is added to the catalogue"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("bookInstance", _.bookInstance),
      ).mapN(BookInstanceAddedToCatalogue.apply)
    }
