package es.eriktorr.library
package lending.model

import shared.UUIDValidationError.UUIDInvalidFormat
import shared.validated.AllErrorsOr

import cats.syntax.all.*

import java.util.UUID
import scala.util.Try

opaque type LibraryBranchId = UUID

object LibraryBranchId:
  def from(value: UUID): LibraryBranchId = value

  def from(value: String): AllErrorsOr[LibraryBranchId] =
    Try(UUID.fromString(value))
      .fold(error => UUIDInvalidFormat(error).invalidNec[UUID], _.nn.validNec)

  extension (libraryBranchId: LibraryBranchId)
    def value: UUID = libraryBranchId
    def asString: String = libraryBranchId.value.toString

  val nil: LibraryBranchId = from(UUID.fromString("00000000-0000-0000-0000-000000000000").nn)
