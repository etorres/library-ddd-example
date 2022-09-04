package es.eriktorr.library
package lending.infrastructure

import lending.model.LibraryBranchId
import shared.infrastructure.StringFieldJsonDecoder
import shared.validated.ValidatedEither.validatedNecEither

import io.circe.{Decoder, Encoder, Json}

trait LibraryBranchIdJsonCodec extends StringFieldJsonDecoder:
  implicit val libraryBranchIdJsonDecoder: Decoder[LibraryBranchId] =
    decodeValue[LibraryBranchId](LibraryBranchId.from(_).either)

  implicit val libraryBranchIdJsonEncoder: Encoder[LibraryBranchId] =
    (libraryBranchId: LibraryBranchId) => Json.fromString(libraryBranchId.asString)
