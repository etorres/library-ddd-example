package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.{BookIdAvroCodec, BookTypeAvroCodec}
import lending.model.BookStateChanged
import lending.model.BookStateChanged.*
import shared.Namespaces
import shared.infrastructure.EventIdAvroCodec

import cats.syntax.apply.*
import cats.syntax.semigroup.*
import vulcan.Codec

trait BookStateChangedAvroCodec
    extends BookTypeAvroCodec
    with BookIdAvroCodec
    with EventIdAvroCodec
    with LibraryBranchIdAvroCodec
    with PatronIdAvroCodec:

  implicit val bookCheckedOutAvroCodec: Codec[BookCheckedOut] =
    Codec.record(
      name = "BookCheckedOut",
      namespace = Namespaces.default,
      doc = Some("An event emitted when a book change its state to checked out"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("patronId", _.patronId),
        field("bookId", _.bookId),
        field("bookType", _.bookType, default = None),
        field("libraryBranchId", _.libraryBranchId),
        field("till", _.till, default = None),
      ).mapN(BookCheckedOut.apply)
    }

  implicit val bookHoldCanceledAvroCodec: Codec[BookHoldCanceled] =
    Codec.record(
      name = "BookHoldCanceled",
      namespace = Namespaces.default,
      doc = Some("An event emitted when a book hold is canceled"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("patronId", _.patronId),
        field("bookId", _.bookId),
        field("libraryBranchId", _.libraryBranchId),
      ).mapN(BookHoldCanceled.apply)
    }

  implicit val bookHoldExpiredAvroCodec: Codec[BookHoldExpired] =
    Codec.record(
      name = "BookHoldExpired",
      namespace = Namespaces.default,
      doc = Some("An event emitted when a book hold expires"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("patronId", _.patronId),
        field("bookId", _.bookId),
        field("libraryBranchId", _.libraryBranchId),
      ).mapN(BookHoldExpired.apply)
    }

  implicit val bookPlacedOnHoldAvroCodec: Codec[BookPlacedOnHold] =
    Codec.record(
      name = "BookPlacedOnHold",
      namespace = Namespaces.default,
      doc = Some("An event emitted when a book is placed on hold"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("patronId", _.patronId),
        field("bookId", _.bookId),
        field("bookType", _.bookType, default = None),
        field("libraryBranchId", _.libraryBranchId),
        field("holdFrom", _.holdFrom, default = None),
        field("holdTill", _.holdTill, default = Some(None)),
      ).mapN(BookPlacedOnHold.apply)
    }

  implicit val bookReturnedAvroCodec: Codec[BookReturned] =
    Codec.record(
      name = "BookReturned",
      namespace = Namespaces.default,
      doc = Some("An event emitted when a book is returned"),
    ) { field =>
      (
        field("eventId", _.eventId),
        field("when", _.when),
        field("patronId", _.patronId),
        field("bookId", _.bookId),
        field("bookType", _.bookType, default = None),
        field("libraryBranchId", _.libraryBranchId),
      ).mapN(BookReturned.apply)
    }

  implicit val bookStateChangedAvroCodec: Codec[BookStateChanged] = Codec.union[BookStateChanged] {
    // @formatter:off      
    alt => alt[BookCheckedOut] |+| alt[BookHoldCanceled] |+| alt[BookHoldExpired] |+| 
      alt[BookPlacedOnHold] |+| alt[BookReturned]
    // @formatter:on
  }
