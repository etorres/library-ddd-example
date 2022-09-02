package es.eriktorr.library
package lending

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import lending.model.BookStateChanged
import lending.model.BookDuplicateHoldFound
import lending.infrastructure.{BookDuplicateHoldFoundAvroCodec, BookStateChangedAvroCodec}
import shared.infrastructure.JdbcTransactor
import shared.infrastructure.KafkaClients
import shared.infrastructure.KafkaClients.{KafkaConsumerIO, KafkaProducerIO}

import cats.effect.{IO, Resource}
import doobie.Transactor
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

final case class LendingResources(
    bookInstanceAddedToCatalogueConsumer: KafkaConsumerIO[BookInstanceAddedToCatalogue],
    bookStateChangedConsumer: KafkaConsumerIO[BookStateChanged],
    bookDuplicateHoldFoundProducer: KafkaProducerIO[BookDuplicateHoldFound],
    jdbcTransactor: Transactor[IO],
)

object LendingResources
    extends BookDuplicateHoldFoundAvroCodec
    with BookInstanceAddedToCatalogueAvroCodec
    with BookStateChangedAvroCodec:
  def impl(
      configuration: LendingConfiguration,
      executionContext: ExecutionContext,
  ): Resource[IO, LendingResources] =
    for
      bookInstanceAddedToCatalogueConsumer <- KafkaClients
        .kafkaConsumerUsing[BookInstanceAddedToCatalogue](
          configuration.bookInstancesKafkaConfig,
        )
      bookStateChangedConsumer <- KafkaClients
        .kafkaConsumerUsing[BookStateChanged](
          configuration.bookStateChangesKafkaConfig,
        )
      bookDuplicateHoldFoundProducer <- KafkaClients
        .kafkaProducerUsing[BookDuplicateHoldFound](
          configuration.bookStateErrorsKafkaConfig,
        )
      jdbcTransactor <- JdbcTransactor(
        configuration.jdbcConfig,
        executionContext,
      ).transactorResource
    yield LendingResources(
      bookInstanceAddedToCatalogueConsumer,
      bookStateChangedConsumer,
      bookDuplicateHoldFoundProducer,
      jdbcTransactor,
    )
