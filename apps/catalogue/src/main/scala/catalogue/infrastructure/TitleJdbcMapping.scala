package es.eriktorr.library
package catalogue.infrastructure

import catalogue.model.Title

import doobie.{Put, Read}

trait TitleJdbcMapping:
  implicit val titlePut: Put[Title] = Put[String].contramap(_.value)
  implicit val titleRead: Read[Title] = Read[String].map(Title.unsafeFrom)
