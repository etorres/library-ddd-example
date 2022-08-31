package es.eriktorr.library
package book.infrastructure

import book.model.BookType

import doobie.{Put, Read}

trait BookTypeJdbcMapping:
  implicit val bookTypePut: Put[BookType] = Put[String].contramap(_.toString)
  implicit val bookTypeRead: Read[BookType] = Read[String].map(BookType.valueOf)
