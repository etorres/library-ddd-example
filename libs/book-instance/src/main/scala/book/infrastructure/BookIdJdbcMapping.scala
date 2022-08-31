package es.eriktorr.library
package book.infrastructure

import book.model.BookId

import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.{Put, Read}

import java.util.UUID

trait BookIdJdbcMapping:
  implicit val bookIdPut: Put[BookId] = Put[UUID].contramap(_.value)
  implicit val bookIdRead: Read[BookId] = Read[UUID].map(BookId.from)
