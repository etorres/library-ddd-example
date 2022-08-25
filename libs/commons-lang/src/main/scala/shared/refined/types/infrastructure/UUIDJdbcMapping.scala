package es.eriktorr.library
package shared.refined.types.infrastructure

import shared.refined.types.UUID

import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.{Put, Read}

trait UUIDJdbcMapping:
  implicit val uuidPut: Put[UUID] = Put[java.util.UUID].contramap(_.asJava)
  implicit val uuidRead: Read[UUID] = Read[java.util.UUID].map(UUID.fromJava)
