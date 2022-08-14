package es.eriktorr.library
package infrastructure.jdbc

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor

import scala.concurrent.ExecutionContext

final class JdbcTransactor(jdbcConfig: JdbcConfig, connectEc: ExecutionContext):
  val transactorResource: Resource[IO, HikariTransactor[IO]] =
    for xa <- HikariTransactor.newHikariTransactor[IO](
        jdbcConfig.driverClassName.value,
        jdbcConfig.connectUrl.value,
        jdbcConfig.user.value,
        jdbcConfig.password.value.value,
        connectEc,
      )
    yield xa
