package es.eriktorr.library
package lending.integration

import lending.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import lending.infrastructure.LendingGenerators.bookInstanceAddedToCatalogueGen

import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll

final class AvroRoundTripSuite extends ScalaCheckSuite with BookInstanceAddedToCatalogueAvroCodec:

  property("book instance added to catalogue event encoding is reversible") {
    forAll(bookInstanceAddedToCatalogueGen) { bookInstanceAddedToCatalogue =>
      assertEquals(
        for
          schema <- bookInstanceAddedToCatalogueAvroCodec.schema
          payload <- bookInstanceAddedToCatalogueAvroCodec.encode(bookInstanceAddedToCatalogue)
          result <- bookInstanceAddedToCatalogueAvroCodec.decode(payload, schema)
        yield result,
        Right(bookInstanceAddedToCatalogue),
      )
    }
  }
