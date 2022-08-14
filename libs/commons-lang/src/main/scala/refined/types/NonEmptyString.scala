package es.eriktorr.library
package refined.types

import refined.types.NonEmptyString.AuthorValidationError.StringIsEmpty

import cats.Show

opaque type NonEmptyString = String

object NonEmptyString:
  def unsafeFrom(value: String): NonEmptyString = value

  def from(value: String): Either[NonEmptyStringValidationError, NonEmptyString] = if value.nonEmpty then
    Right(unsafeFrom(value))
  else Left(StringIsEmpty)

  extension (nonEmptyString: NonEmptyString) def value: String = nonEmptyString

  given Show[NonEmptyString] = Show.show(_.value)

  sealed abstract class NonEmptyStringValidationError(message: String) extends DomainError(message)

  object AuthorValidationError:
    case object StringIsEmpty extends NonEmptyStringValidationError("Author cannot be empty")
