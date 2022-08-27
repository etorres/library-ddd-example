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
