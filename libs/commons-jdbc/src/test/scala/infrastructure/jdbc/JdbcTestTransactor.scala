package es.eriktorr.library
package infrastructure.jdbc

import cats.effect.{IO, Resource}
import cats.implicits.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.{Fragment, Transactor}
import eu.timepit.refined.api.Refined

import scala.concurrent.ExecutionContext

object JdbcTestTransactor:
  def testTransactorResource(
      currentSchema: String,
      connectEc: ExecutionContext,
  ): Resource[IO, HikariTransactor[IO]] = for
    transactor <- JdbcTransactor(
      JdbcTestConfig.jdbcConfig.copy(connectUrl =
        Refined.unsafeApply(
          s"${JdbcTestConfig.jdbcConfig.connectUrl.value}?currentSchema=$currentSchema",
        ),
      ),
      connectEc,
    ).transactorResource
    _ <- truncateAllTablesIn(transactor, currentSchema)
  yield transactor

  private[this] def truncateAllTablesIn(
      transactor: Transactor[IO],
      currentSchema: String,
  ): Resource[IO, Unit] =
    Resource.make {
      (for
        tableNames <- sql"""
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = $currentSchema
            ORDER BY table_name""".query[String].to[List]
        _ <- tableNames
          .map(tableName => Fragment.const(s"truncate table $tableName"))
          .traverse_(_.update.run)
      yield ()).transact(transactor)
    }(_ => IO.unit)
