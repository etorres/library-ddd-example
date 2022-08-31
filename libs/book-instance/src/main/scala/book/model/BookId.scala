package es.eriktorr.library
package book.model

import shared.UUIDValidationError.UUIDInvalidFormat
import shared.validated.AllErrorsOr

import cats.syntax.all.*

import java.util.UUID
import scala.util.Try

opaque type BookId = UUID

object BookId:
  def from(value: UUID): BookId = value

  def from(value: String): AllErrorsOr[BookId] =
    Try(UUID.fromString(value))
      .fold(error => UUIDInvalidFormat(error).invalidNec[UUID], _.nn.validNec)

  extension (bookId: BookId) def value: UUID = bookId
