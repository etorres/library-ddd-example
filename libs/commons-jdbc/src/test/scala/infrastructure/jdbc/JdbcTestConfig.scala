package es.eriktorr.library
package infrastructure.jdbc

import refined.types.NonEmptyString

import ciris.Secret

object JdbcTestConfig:
    val jdbcConfig: JdbcConfig = JdbcConfig(
      NonEmptyString.unsafeFrom("org.postgresql.Driver"),
      NonEmptyString.unsafeFrom("jdbc:postgresql://localhost:5432/library_db"),
      NonEmptyString.unsafeFrom("library_user"),
      Secret(NonEmptyString.unsafeFrom("changeme"))
    )
