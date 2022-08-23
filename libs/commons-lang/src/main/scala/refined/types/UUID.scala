package es.eriktorr.library
package refined.types

import refined.types.UUID.UUIDValidationError.UUIDInvalidFormat
import validated.AllErrorsOr

import cats.Show
import cats.syntax.all.*

opaque type UUID = String

object UUID:
  def unsafeFrom(value: String): UUID = value

  def from(value: String): AllErrorsOr[UUID] =
    if raw"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r.matches(value) then
      value.validNec
    else UUIDInvalidFormat.invalidNec

  extension (uuid: UUID) def value: String = uuid

  given Show[UUID] = Show.show(_.value)

  sealed abstract class UUIDValidationError(message: String) extends ValidationError(message)

  object UUIDValidationError:
    case object UUIDInvalidFormat extends UUIDValidationError("Invalid or unsupported UUID format")
