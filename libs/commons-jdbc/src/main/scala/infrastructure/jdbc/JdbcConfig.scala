package es.eriktorr.library
package infrastructure.jdbc

import shared.refined.types.NonEmptyString

import ciris.Secret

final case class JdbcConfig(
    driverClassName: NonEmptyString,
    connectUrl: NonEmptyString,
    user: NonEmptyString,
    password: Secret[NonEmptyString],
)
