package es.eriktorr.library
package catalogue

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.JdbcTransactor
import shared.infrastructure.KafkaClients
import shared.infrastructure.KafkaClients.KafkaProducerIO

import cats.effect.{IO, Resource}
import doobie.Transactor
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

final case class CatalogueResources(
    bookInstanceAddedToCatalogueProducer: KafkaProducerIO[BookInstanceAddedToCatalogue],
    jdbcTransactor: Transactor[IO],
)

object CatalogueResources extends BookInstanceAddedToCatalogueAvroCodec:
  def impl(
      configuration: CatalogueConfiguration,
      executionContext: ExecutionContext,
  ): Resource[IO, CatalogueResources] =
    for
      bookInstanceAddedToCatalogueProducer <- KafkaClients
        .kafkaProducerUsing[BookInstanceAddedToCatalogue](
          configuration.bookInstancesKafkaConfig,
        )
      jdbcTransactor <- JdbcTransactor(
        configuration.jdbcConfig,
        executionContext,
      ).transactorResource
    yield CatalogueResources(bookInstanceAddedToCatalogueProducer, jdbcTransactor)
