package es.eriktorr.library
package book.unit

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.infrastructure.BookGenerators.bookInstanceAddedToCatalogueGen
import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.AvroRoundTripSuite

import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll

final class BookInstanceAddedToCatalogueAvroSuite
    extends ScalaCheckSuite
    with BookInstanceAddedToCatalogueAvroCodec:

  property("book instance added to catalogue avro encoding is reversible") {
    AvroRoundTripSuite.checkUsing[BookInstanceAddedToCatalogue](
      bookInstanceAddedToCatalogueGen,
      bookInstanceAddedToCatalogueAvroCodec,
    )
  }
