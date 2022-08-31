package es.eriktorr.library
package lending.infrastructure

import lending.model.LibraryBranchId

import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.Meta

import java.util.UUID

trait LibraryBranchIdJdbcMapping:
  implicit val libraryBranchIdMeta: Meta[LibraryBranchId] =
    Meta[UUID].timap(LibraryBranchId.from)(_.value)
