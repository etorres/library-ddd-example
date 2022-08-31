package es.eriktorr.library
package book.infrastructure

import book.model.BookId

import doobie.Meta
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

trait BookIdJdbcMapping:
  implicit val bookIdMeta: Meta[BookId] = Meta[UUID].timap(BookId.from)(_.value)
