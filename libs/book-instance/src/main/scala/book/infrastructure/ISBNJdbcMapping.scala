package es.eriktorr.library
package book.infrastructure

import book.model.ISBN

import doobie.{Put, Read}

trait ISBNJdbcMapping:
  implicit val isbnPut: Put[ISBN] = Put[String].contramap(_.value)
  implicit val isbnRead: Read[ISBN] = Read[String].map(ISBN.unsafeFrom)
