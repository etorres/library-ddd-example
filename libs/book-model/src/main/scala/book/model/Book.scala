package es.eriktorr.library
package book.model

import validated.AllErrorsOr

import cats.syntax.all.*

final case class Book(isbn: ISBN, title: Title, author: Author)

object Book:
  def from(isbn: String, title: String, author: String): AllErrorsOr[Book] =
    (ISBN.from(isbn), Title.from(title), Author.from(author)).mapN(Book.apply)
