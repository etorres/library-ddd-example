package es.eriktorr.library
package lending.infrastructure

import lending.model.BookState

import doobie.Meta

trait BookStateJdbcMapping:
  implicit val bookStateMeta: Meta[BookState] = Meta[String].timap(BookState.valueOf)(_.toString)
