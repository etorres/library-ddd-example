package es.eriktorr.library
package shared.infrastructure

import shared.refined.types.NonEmptyString

import ciris.Secret

final case class JdbcConfig(
    driverClassName: NonEmptyString,
    connectUrl: NonEmptyString,
    user: NonEmptyString,
    password: Secret[NonEmptyString],
)

object JdbcConfig:
    val default: JdbcConfig = JdbcConfig(
      NonEmptyString.unsafeFrom("org.postgresql.Driver"),
      NonEmptyString.unsafeFrom("jdbc:postgresql://localhost:5432/library_db"),
      NonEmptyString.unsafeFrom("library_user"),
      Secret(NonEmptyString.unsafeFrom("changeme"))
    )
