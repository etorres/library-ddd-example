package es.eriktorr.library
package book.model

import book.model.Author.AuthorValidationError.AuthorIsEmpty
import validated.AllErrorsOr

import cats.syntax.all.*

opaque type Author = String

object Author:
  def unsafeFrom(value: String): Author = value

  def from(value: String): AllErrorsOr[Author] =
    if value.nonEmpty then value.validNec else AuthorIsEmpty.invalidNec

  extension (author: Author) def value: String = author

  sealed abstract class AuthorValidationError(message: String) extends ValidationError(message)

  object AuthorValidationError:
    case object AuthorIsEmpty extends AuthorValidationError("Author cannot be empty")
