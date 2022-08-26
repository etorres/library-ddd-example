package es.eriktorr.library
package lending

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.JdbcTransactor
import shared.infrastructure.KafkaClients
import shared.infrastructure.KafkaClients.KafkaConsumerIO

import cats.effect.{IO, Resource}
import doobie.Transactor
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

final case class LendingResources(
    bookInstanceAddedToCatalogueConsumer: KafkaConsumerIO[BookInstanceAddedToCatalogue],
    jdbcTransactor: Transactor[IO],
)

object LendingResources extends BookInstanceAddedToCatalogueAvroCodec:
  def impl(
      configuration: LendingConfiguration,
      executionContext: ExecutionContext,
  ): Resource[IO, LendingResources] =
    for
      jdbcTransactor <- JdbcTransactor(
        configuration.jdbcConfig,
        executionContext,
      ).transactorResource
      bookInstanceAddedToCatalogueConsumer <- KafkaClients
        .kafkaConsumerUsing[BookInstanceAddedToCatalogue](
          configuration.kafkaConfig,
        )
    yield LendingResources(bookInstanceAddedToCatalogueConsumer, jdbcTransactor)
