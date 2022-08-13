package es.eriktorr.library
package catalogue.infrastructure

import book.model.Title

import doobie.{Put, Read}

trait TitleMapping:
  implicit val titlePut: Put[Title] = Put[String].contramap(_.value)
  implicit val titleRead: Read[Title] = Read[String].map(Title.unsafeFrom)
