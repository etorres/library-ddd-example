package es.eriktorr.library
package catalogue.infrastructure

import catalogue.model.Title

import doobie.Meta

trait TitleJdbcMapping:
  implicit val titleMeta: Meta[Title] = Meta[String].timap(Title.unsafeFrom)(_.value)
