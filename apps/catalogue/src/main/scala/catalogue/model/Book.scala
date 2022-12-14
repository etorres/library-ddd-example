package es.eriktorr.library
package catalogue.model

import book.model.ISBN
import shared.validated.AllErrorsOr

import cats.syntax.all.*

final case class Book(isbn: ISBN, title: Title, author: Author)

object Book:
  def from(isbn: String, title: String, author: String): AllErrorsOr[Book] =
    (ISBN.from(isbn), Title.from(title), Author.from(author)).mapN(Book.apply)
