package es.eriktorr.library
package lending

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import lending.model.BookStateChange.{
  BookCheckedOut,
  BookHoldCanceled,
  BookHoldExpired,
  BookPlacedOnHold,
  BookReturned,
}
import lending.model.BookDuplicateHoldFound
import lending.infrastructure.{BookDuplicateHoldFoundAvroCodec, BookStateChangedAvroCodecs}
import shared.infrastructure.JdbcTransactor
import shared.infrastructure.KafkaClients
import shared.infrastructure.KafkaClients.{KafkaConsumerIO, KafkaProducerIO}

import cats.effect.{IO, Resource}
import doobie.Transactor
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

final case class LendingResources(
    bookInstanceAddedToCatalogueConsumer: KafkaConsumerIO[BookInstanceAddedToCatalogue],
    bookPlacedOnHoldConsumer: KafkaConsumerIO[BookPlacedOnHold],
    bookCheckedOutConsumer: KafkaConsumerIO[BookCheckedOut],
    bookHoldExpiredConsumer: KafkaConsumerIO[BookHoldExpired],
    bookHoldCanceledConsumer: KafkaConsumerIO[BookHoldCanceled],
    bookReturnedConsumer: KafkaConsumerIO[BookReturned],
    bookDuplicateHoldFoundProducer: KafkaProducerIO[BookDuplicateHoldFound],
    jdbcTransactor: Transactor[IO],
)

object LendingResources
    extends BookDuplicateHoldFoundAvroCodec
    with BookInstanceAddedToCatalogueAvroCodec
    with BookStateChangedAvroCodecs:
  def impl(
      configuration: LendingConfiguration,
      executionContext: ExecutionContext,
  ): Resource[IO, LendingResources] =
    for
      bookInstanceAddedToCatalogueConsumer <- KafkaClients
        .kafkaConsumerUsing[BookInstanceAddedToCatalogue](
          configuration.kafkaConfig,
        )
      bookPlacedOnHoldConsumer <- KafkaClients
        .kafkaConsumerUsing[BookPlacedOnHold](
          configuration.kafkaConfig,
        )
      bookCheckedOutConsumer <- KafkaClients
        .kafkaConsumerUsing[BookCheckedOut](
          configuration.kafkaConfig,
        )
      bookHoldExpiredConsumer <- KafkaClients
        .kafkaConsumerUsing[BookHoldExpired](
          configuration.kafkaConfig,
        )
      bookHoldCanceledConsumer <- KafkaClients
        .kafkaConsumerUsing[BookHoldCanceled](
          configuration.kafkaConfig,
        )
      bookReturnedConsumer <- KafkaClients
        .kafkaConsumerUsing[BookReturned](
          configuration.kafkaConfig,
        )
      bookDuplicateHoldFoundProducer <- KafkaClients
        .kafkaProducerUsing[BookDuplicateHoldFound](
          configuration.kafkaConfig,
        )
      jdbcTransactor <- JdbcTransactor(
        configuration.jdbcConfig,
        executionContext,
      ).transactorResource
    yield LendingResources(
      bookInstanceAddedToCatalogueConsumer,
      bookPlacedOnHoldConsumer,
      bookCheckedOutConsumer,
      bookHoldExpiredConsumer,
      bookHoldCanceledConsumer,
      bookReturnedConsumer,
      bookDuplicateHoldFoundProducer,
      jdbcTransactor,
    )
