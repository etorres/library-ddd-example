package es.eriktorr.library
package shared.refined.types

import shared.ValidationError
import shared.refined.types.UUID.UUIDValidationError.UUIDInvalidFormat
import shared.validated.AllErrorsOr

import cats.Show
import cats.syntax.all.*

opaque type UUID = String

object UUID:
  def unsafeFrom(value: String): UUID = value

  def from(value: String): AllErrorsOr[UUID] =
    if raw"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".r.matches(value) then
      value.validNec
    else UUIDInvalidFormat.invalidNec

  def fromJava(value: java.util.UUID): UUID = unsafeFrom(value.toString.toLowerCase.nn)

  extension (uuid: UUID)
    def value: String = uuid
    def asJava: java.util.UUID = java.util.UUID.fromString(uuid.value).nn

  given Show[UUID] = Show.show(_.value)

  val nil: UUID = unsafeFrom("00000000-0000-0000-0000-000000000000")

  sealed abstract class UUIDValidationError(message: String) extends ValidationError(message)

  object UUIDValidationError:
    case object UUIDInvalidFormat extends UUIDValidationError("Invalid or unsupported UUID format")
