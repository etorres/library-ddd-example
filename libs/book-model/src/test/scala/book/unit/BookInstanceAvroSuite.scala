package es.eriktorr.library
package book.unit

import book.infrastructure.BookGenerators.bookInstanceGen
import book.infrastructure.BookInstanceAvroCodec
import book.model.BookInstance
import shared.infrastructure.AvroRoundTripSuite

import munit.ScalaCheckSuite

final class BookInstanceAvroSuite extends ScalaCheckSuite with BookInstanceAvroCodec:

  property("book instance avro encoding is reversible") {
    AvroRoundTripSuite.checkUsing[BookInstance](
      bookInstanceGen(),
      bookInstanceAvroCodec,
    )
  }
