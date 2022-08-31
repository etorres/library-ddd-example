package es.eriktorr.library
package shared

import shared.UUIDValidationError.UUIDInvalidFormat
import shared.validated.AllErrorsOr

import cats.syntax.all.*

import java.util.UUID
import scala.util.Try

opaque type EventId = UUID

object EventId:
  def from(value: UUID): EventId = value

  def from(value: String): AllErrorsOr[EventId] =
    Try(UUID.fromString(value))
      .fold(error => UUIDInvalidFormat(error).invalidNec[UUID], _.nn.validNec)

  extension (bookId: EventId)
    def value: UUID = bookId
    def asString: String = bookId.toString
