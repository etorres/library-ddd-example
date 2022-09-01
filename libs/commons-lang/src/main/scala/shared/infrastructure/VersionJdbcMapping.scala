package es.eriktorr.library
package shared.infrastructure

import shared.Version

import doobie.Meta

trait VersionJdbcMapping:
  implicit val versionMeta: Meta[Version] = Meta[Long].timap(Version.from)(_.value)
