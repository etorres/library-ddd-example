package es.eriktorr.library
package lending.infrastructure

import lending.model.LibraryBranchId

import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.{Put, Read}

import java.util.UUID

trait LibraryBranchIdJdbcMapping:
  implicit val libraryBranchIdPut: Put[LibraryBranchId] = Put[UUID].contramap(_.value)
  implicit val libraryBranchIdRead: Read[LibraryBranchId] = Read[UUID].map(LibraryBranchId.from)
