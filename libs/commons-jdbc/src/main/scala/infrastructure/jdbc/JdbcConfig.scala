package es.eriktorr.library
package infrastructure.jdbc

import ciris.Secret
import eu.timepit.refined.types.string.NonEmptyString

final case class JdbcConfig(
    driverClassName: NonEmptyString,
    connectUrl: NonEmptyString,
    user: NonEmptyString,
    password: Secret[NonEmptyString],
)
