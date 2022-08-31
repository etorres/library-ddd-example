package es.eriktorr.library
package lending.model

import shared.UUIDValidationError.UUIDInvalidFormat
import shared.validated.AllErrorsOr

import cats.syntax.all.*

import java.util.UUID
import scala.util.Try

opaque type PatronId = UUID

object PatronId:
  def from(value: UUID): PatronId = value

  def from(value: String): AllErrorsOr[PatronId] =
    Try(UUID.fromString(value))
      .fold(error => UUIDInvalidFormat(error).invalidNec[UUID], _.nn.validNec)

  extension (patronId: PatronId) def value: UUID = patronId
