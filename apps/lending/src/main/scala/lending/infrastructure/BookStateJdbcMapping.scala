package es.eriktorr.library
package lending.infrastructure

import lending.model.BookState
import doobie.{Put, Read}

trait BookStateJdbcMapping:
  implicit val bookStatePut: Put[BookState] = Put[String].contramap(_.toString)
  implicit val bookStateRead: Read[BookState] = Read[String].map(BookState.valueOf)
