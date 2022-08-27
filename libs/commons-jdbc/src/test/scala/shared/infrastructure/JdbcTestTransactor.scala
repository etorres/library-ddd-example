package es.eriktorr.library
package shared.infrastructure

import shared.refined.types.NonEmptyString

import cats.effect.{IO, Resource}
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.{Fragment, Transactor}

import scala.concurrent.ExecutionContext

object JdbcTestTransactor:
  def testTransactorResource(
      jdbcTestConfig: JdbcTestConfig,
      connectEc: ExecutionContext,
  ): Resource[IO, HikariTransactor[IO]] = for
    transactor <- JdbcTransactor(
      jdbcTestConfig.jdbcConfig.copy(connectUrl =
        NonEmptyString.unsafeFrom(
          s"${jdbcTestConfig.jdbcConfig.connectUrl.value}?currentSchema=${jdbcTestConfig.schema.value}",
        ),
      ),
      connectEc,
    ).transactorResource
    _ <- truncateAllTablesIn(transactor, jdbcTestConfig.schema)
  yield transactor

  private[this] def truncateAllTablesIn(
      transactor: Transactor[IO],
      schema: NonEmptyString,
  ): Resource[IO, Unit] =
    Resource.make {
      (for
        tableNames <- sql"""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = ${schema.value}
            ORDER BY table_name""".query[String].to[List]
        _ <- tableNames
          .map(tableName => Fragment.const(s"TRUNCATE TABLE $tableName CASCADE"))
          .traverse_(_.update.run)
      yield ()).transact(transactor)
    }(_ => IO.unit)
