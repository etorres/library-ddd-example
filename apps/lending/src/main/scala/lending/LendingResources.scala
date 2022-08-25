package es.eriktorr.library
package lending

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import book.model.BookInstanceAddedToCatalogue
import lending.application.CreateAvailableBookOnInstanceAdded
import lending.model.{AvailableBooks, BookInstanceAddedToCatalogueEventHandler}
import shared.infrastructure.KafkaClients
import shared.refined.types.UUID

final class LendingResources(
    availableBooks: AvailableBooks,
    bookInstanceAddedToCatalogueEventHandler: BookInstanceAddedToCatalogueEventHandler,
    libraryBranchId: UUID,
):
  val createAvailableBookOnInstanceAdded: CreateAvailableBookOnInstanceAdded =
    CreateAvailableBookOnInstanceAdded(
      availableBooks,
      bookInstanceAddedToCatalogueEventHandler,
      libraryBranchId,
    )

object LendingResources extends BookInstanceAddedToCatalogueAvroCodec:
  def impl(configuration: LendingConfiguration, parameters: LendingParameters) = for _ <-
      KafkaClients
        .kafkaConsumerUsing[BookInstanceAddedToCatalogue](configuration.kafkaConfig)
        .allocated
  yield LendingResources(???, ???, ???)
