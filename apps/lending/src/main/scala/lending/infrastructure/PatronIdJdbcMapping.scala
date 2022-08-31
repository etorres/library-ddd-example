package es.eriktorr.library
package lending.infrastructure

import lending.model.PatronId

import doobie.Meta
import doobie.postgres.*
import doobie.postgres.implicits.*

import java.util.UUID

trait PatronIdJdbcMapping:
  implicit val patronIdMeta: Meta[PatronId] = Meta[UUID].timap(PatronId.from)(_.value)
