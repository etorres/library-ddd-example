package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookIdAvroCodec
import lending.model.BookDuplicateHoldFound
import shared.Namespaces
import shared.infrastructure.EventIdAvroCodec

import cats.syntax.apply.*
import vulcan.Codec

trait BookDuplicateHoldFoundAvroCodec 
    extends BookIdAvroCodec
    with EventIdAvroCodec
    with LibraryBranchIdAvroCodec
    with PatronIdAvroCodec:
  implicit val bookDuplicateHoldFoundAvroCodec: Codec[BookDuplicateHoldFound] = 
    Codec.record(
      name = "BookDuplicateHoldFound",
      namespace = Namespaces.default,
      doc = Some("An event emitted when a book hold is duplicated"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("firstPatronId", _.firstPatronId),
        field("secondPatronId", _.secondPatronId),
        field("libraryBranchId", _.libraryBranchId),
        field("bookId", _.bookId),
      ).mapN(BookDuplicateHoldFound.apply)
    }
