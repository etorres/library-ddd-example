package es.eriktorr.library
package shared.refined.types

import shared.ValidationError
import shared.refined.types.NonEmptyString.NonEmptyStringValidationError.StringIsEmpty
import shared.validated.AllErrorsOr

import cats.Show
import cats.syntax.all.*

opaque type NonEmptyString = String

object NonEmptyString:
  def unsafeFrom(value: String): NonEmptyString = value

  def from(value: String): AllErrorsOr[NonEmptyString] =
    if value.nonEmpty then value.validNec else StringIsEmpty.invalidNec

  extension (nonEmptyString: NonEmptyString) def value: String = nonEmptyString

  given Show[NonEmptyString] = Show.show(_.value)

  sealed abstract class NonEmptyStringValidationError(message: String)
      extends ValidationError(message)

  object NonEmptyStringValidationError:
    case object StringIsEmpty extends NonEmptyStringValidationError("Value cannot be empty")
