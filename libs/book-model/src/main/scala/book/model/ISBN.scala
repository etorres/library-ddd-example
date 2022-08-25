package es.eriktorr.library
package book.model

import book.model.ISBN.ISBNValidationError.ISBNInvalidFormat
import shared.ValidationError
import shared.validated.AllErrorsOr

import cats.syntax.all.*

opaque type ISBN = String

object ISBN:
  def unsafeFrom(value: String): ISBN = value

  def from(value: String): AllErrorsOr[ISBN] =
    if raw"\d{9}[\d|X]".r.matches(value) then value.validNec else ISBNInvalidFormat.invalidNec

  extension (isbn: ISBN) def value: String = isbn

  sealed abstract class ISBNValidationError(message: String) extends ValidationError(message)

  object ISBNValidationError:
    case object ISBNInvalidFormat extends ISBNValidationError("Invalid or unsupported ISBN format")
