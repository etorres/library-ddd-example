package es.eriktorr.library
package lending.infrastructure

import lending.model.PatronType

import doobie.Meta

trait PatronTypeJdbcMapping:
  implicit val patronTypeMeta: Meta[PatronType] = Meta[String].timap(PatronType.valueOf)(_.toString)
