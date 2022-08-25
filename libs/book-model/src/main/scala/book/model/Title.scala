package es.eriktorr.library
package book.model

import book.model.Title.TitleValidationError.TitleIsEmpty
import shared.ValidationError
import shared.validated.AllErrorsOr

import cats.syntax.all.*

opaque type Title = String

object Title:
  def unsafeFrom(value: String): Title = value

  def from(value: String): AllErrorsOr[Title] =
    if value.nonEmpty then value.validNec else TitleIsEmpty.invalidNec

  extension (title: Title) def value: String = title

  sealed abstract class TitleValidationError(message: String) extends ValidationError(message)

  object TitleValidationError:
    case object TitleIsEmpty extends TitleValidationError("Title cannot be empty")
