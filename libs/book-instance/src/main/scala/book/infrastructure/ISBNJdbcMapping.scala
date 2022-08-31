package es.eriktorr.library
package book.infrastructure

import book.model.ISBN

import doobie.Meta

trait ISBNJdbcMapping:
  implicit val isbnMeta: Meta[ISBN] = Meta[String].timap(ISBN.unsafeFrom)(_.value)
