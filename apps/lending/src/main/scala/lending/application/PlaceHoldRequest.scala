package es.eriktorr.library
package lending.application

import book.model.BookId
import lending.infrastructure.{BookIdJsonCodec, LibraryBranchIdJsonCodec}
import lending.model.LibraryBranchId

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder, HCursor}

final case class PlaceHoldRequest(
    bookId: BookId,
    libraryBranchId: LibraryBranchId,
    numberOfDays: Option[Int],
)

object PlaceHoldRequest extends BookIdJsonCodec with LibraryBranchIdJsonCodec:
  implicit val placeHoldRequestJsonDecoder: Decoder[PlaceHoldRequest] = (cursor: HCursor) =>
    for
      bookId <- cursor.downField("bookId").as[BookId]
      libraryBranchId <- cursor.downField("libraryBranchId").as[LibraryBranchId]
      numberOfDays <- cursor.downField("numberOfDays").as[Option[Int]]
    yield PlaceHoldRequest(bookId, libraryBranchId, numberOfDays)

  implicit val placeHoldRequestJsonEncoder: Encoder[PlaceHoldRequest] = deriveEncoder
