package es.eriktorr.library
package book.model

import book.model.Author.AuthorValidationError.AuthorIsEmpty

opaque type Author = String

object Author:
  def unsafeFrom(value: String): Author = value

  def from(value: String): Either[AuthorValidationError, Author] = if value.nonEmpty then
    Right(unsafeFrom(value))
  else Left(AuthorIsEmpty)

  extension (author: Author) def value: String = author

  sealed abstract class AuthorValidationError(message: String) extends DomainError(message)

  object AuthorValidationError:
    case object AuthorIsEmpty extends AuthorValidationError("Author cannot be empty")
