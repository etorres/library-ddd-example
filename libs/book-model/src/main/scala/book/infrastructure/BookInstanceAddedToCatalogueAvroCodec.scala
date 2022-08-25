package es.eriktorr.library
package book.infrastructure

import book.model.BookInstanceAddedToCatalogue
import shared.Namespaces
import shared.refined.types.infrastructure.UUIDAvroCodec

import cats.syntax.apply.*
import vulcan.{AvroError, Codec}

trait BookInstanceAddedToCatalogueAvroCodec extends BookInstanceAvroCodec with UUIDAvroCodec:
  implicit val bookInstanceAddedToCatalogueAvroCodec: Codec[BookInstanceAddedToCatalogue] =
    Codec.record(
      name = "BookInstanceAddedToCatalogue",
      namespace = Namespaces.default,
      doc = Some("An event emitted everytime a new book instance is added to the catalogue"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("bookInstance", _.bookInstance),
      ).mapN(BookInstanceAddedToCatalogue.apply)
    }
