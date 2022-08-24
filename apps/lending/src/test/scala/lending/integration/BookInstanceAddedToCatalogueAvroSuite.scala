package es.eriktorr.library
package lending.integration

import book.infrastructure.AvroRoundTripSuite
import lending.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import lending.infrastructure.LendingGenerators.bookInstanceAddedToCatalogueGen
import lending.model.BookInstanceAddedToCatalogue

import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll

final class BookInstanceAddedToCatalogueAvroSuite
    extends ScalaCheckSuite
    with BookInstanceAddedToCatalogueAvroCodec:

  property("book instance added to catalogue event encoding is reversible") {
    AvroRoundTripSuite.checkUsing[BookInstanceAddedToCatalogue](
      bookInstanceAddedToCatalogueGen,
      bookInstanceAddedToCatalogueAvroCodec,
    )
  }
