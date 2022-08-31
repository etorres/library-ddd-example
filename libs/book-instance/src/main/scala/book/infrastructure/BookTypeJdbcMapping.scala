package es.eriktorr.library
package book.infrastructure

import book.model.BookType

import doobie.Meta

trait BookTypeJdbcMapping:
  implicit val bookTypeMeta: Meta[BookType] = Meta[String].timap(BookType.valueOf)(_.toString)
