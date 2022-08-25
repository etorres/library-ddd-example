package es.eriktorr.library
package lending

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import lending.application.CreateAvailableBookOnInstanceAdded
import lending.model.{AvailableBooks, BookInstanceAddedToCatalogueEventHandler}
import shared.infrastructure.JdbcTransactor
import shared.infrastructure.KafkaClients
import shared.infrastructure.KafkaClients.KafkaConsumerIO
import shared.refined.types.UUID

import cats.effect.IO
import doobie.Transactor
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext

//final class LendingResources(
//    availableBooks: AvailableBooks,
//    bookInstanceAddedToCatalogueEventHandler: BookInstanceAddedToCatalogueEventHandler,
//    libraryBranchId: UUID,
//):
//  val createAvailableBookOnInstanceAdded: CreateAvailableBookOnInstanceAdded =
//    CreateAvailableBookOnInstanceAdded(
//      availableBooks,
//      bookInstanceAddedToCatalogueEventHandler,
//      libraryBranchId,
//    )

final case class LendingResources(
    kafkaConsumer: KafkaConsumerIO[BookInstanceAddedToCatalogue],
    jdbcTransactor: Transactor[IO],
)

object LendingResources extends BookInstanceAddedToCatalogueAvroCodec:
  def impl(
      configuration: LendingConfiguration,
      executionContext: ExecutionContext,
  ) =
    for
      jdbcTransactor <- JdbcTransactor(
        configuration.jdbcConfig,
        executionContext,
      ).transactorResource
      kafkaConsumer <- KafkaClients.kafkaConsumerUsing[BookInstanceAddedToCatalogue](
        configuration.kafkaConfig,
      )
    yield LendingResources(kafkaConsumer, jdbcTransactor)
