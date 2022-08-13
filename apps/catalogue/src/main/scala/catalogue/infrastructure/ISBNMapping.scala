package es.eriktorr.library
package catalogue.infrastructure

import book.model.ISBN

import doobie.{Put, Read}

trait ISBNMapping:
  implicit val isbnPut: Put[ISBN] = Put[String].contramap(_.value)
  implicit val isbnRead: Read[ISBN] = Read[String].map(ISBN.unsafeFrom)
