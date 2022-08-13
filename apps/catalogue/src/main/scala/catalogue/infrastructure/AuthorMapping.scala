package es.eriktorr.library
package catalogue.infrastructure

import book.model.Author

import doobie.{Put, Read}

trait AuthorMapping:
  implicit val authorPut: Put[Author] = Put[String].contramap(_.value)
  implicit val authorRead: Read[Author] = Read[String].map(Author.unsafeFrom)
