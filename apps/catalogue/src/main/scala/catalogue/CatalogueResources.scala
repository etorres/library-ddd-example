package es.eriktorr.library
package catalogue

import shared.infrastructure.JdbcTransactor

import cats.effect.{IO, Resource}
import doobie.Transactor

import scala.concurrent.ExecutionContext

final case class CatalogueResources(jdbcTransactor: Transactor[IO])

object CatalogueResources:
    def impl(
      configuration: CatalogueConfiguration,
      executionContext: ExecutionContext,
  ): Resource[IO, CatalogueResources] = 
    for
      jdbcTransactor <- JdbcTransactor(
        configuration.jdbcConfig,
        executionContext,
      ).transactorResource
    yield CatalogueResources(jdbcTransactor)
