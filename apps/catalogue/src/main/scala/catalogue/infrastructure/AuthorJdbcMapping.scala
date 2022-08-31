package es.eriktorr.library
package catalogue.infrastructure

import catalogue.model.Author

import doobie.Meta

trait AuthorJdbcMapping:
  implicit val authorMeta: Meta[Author] = Meta[String].timap(Author.unsafeFrom)(_.value)
