package es.eriktorr.library
package book.model

import book.model.Title.TitleValidationError.TitleIsEmpty

opaque type Title = String

object Title:
  def unsafeFrom(value: String): Title = value

  def from(value: String): Either[TitleValidationError, Title] = if value.nonEmpty then
    Right(unsafeFrom(value))
  else Left(TitleIsEmpty)

  extension (title: Title) def value: String = title

  sealed abstract class TitleValidationError(message: String) extends DomainError(message)

  object TitleValidationError:
    case object TitleIsEmpty extends TitleValidationError("Title cannot be empty")
