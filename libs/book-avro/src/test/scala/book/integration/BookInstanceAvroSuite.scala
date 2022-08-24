package es.eriktorr.library
package book.integration

import book.infrastructure.{AvroRoundTripSuite, BookInstanceAvroCodec}
import book.model.BookInstance
import es.eriktorr.library.book.infrastructure.BookGenerators.bookInstanceGen

import munit.ScalaCheckSuite

final class BookInstanceAvroSuite extends ScalaCheckSuite with BookInstanceAvroCodec:

  property("book instance encoding is reversible") {
    AvroRoundTripSuite.checkUsing[BookInstance](
      bookInstanceGen(),
      bookInstanceAvroCodec,
    )
  }
