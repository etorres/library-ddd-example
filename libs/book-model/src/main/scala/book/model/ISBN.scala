package es.eriktorr.library
package book.model

import book.model.ISBN.ISBNValidationError.ISBNInvalidFormat

opaque type ISBN = String

object ISBN:
  def unsafeFrom(value: String): ISBN = value

  def from(value: String): Either[ISBNValidationError, ISBN] =
    if raw"\\d{9}[\\d|X]".r.matches(value) then Right(unsafeFrom(value))
    else Left(ISBNInvalidFormat)

  extension (isbn: ISBN) def value: String = isbn

  sealed abstract class ISBNValidationError(
      message: String,
      cause: Option[Throwable] = Option.empty[Throwable],
  ) extends DomainError(message, cause)

  object ISBNValidationError:
    case object ISBNInvalidFormat extends ISBNValidationError("Invalid or unsupported ISBN format")
