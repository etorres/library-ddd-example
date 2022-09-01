package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.{BookIdAvroCodec, BookTypeAvroCodec}
import lending.model.BookStateChange.*
import shared.Namespaces
import shared.infrastructure.EventIdAvroCodec

import cats.syntax.apply.*
import vulcan.Codec

trait BookStateChangedAvroCodecs
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
        field("bookType", _.bookType),
        field("libraryBranchId", _.libraryBranchId),
        field("till", _.till),
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
        field("bookType", _.bookType),
        field("libraryBranchId", _.libraryBranchId),
        field("holdFrom", _.holdFrom),
        field("holdTill", _.holdTill),
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
        field("bookType", _.bookType),
        field("libraryBranchId", _.libraryBranchId),
      ).mapN(BookReturned.apply)
    }
