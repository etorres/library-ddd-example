package es.eriktorr.library
package lending.infrastructure

import lending.model.LibraryBranchId

import vulcan.Codec

trait LibraryBranchIdAvroCodec:
  implicit val libraryBranchIdAvroCodec: Codec[LibraryBranchId] =
    Codec.uuid.imap(LibraryBranchId.from)(_.value)
