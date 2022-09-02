package es.eriktorr.library
package lending.integration

import book.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import shared.infrastructure.KafkaTestConfig
import shared.infrastructure.TestFilters.online

import fs2.kafka.vulcan.SchemaRegistryClientSettings
import fs2.kafka.vulcan.testkit.SchemaSuite
import munit.CatsEffectSuite
import org.apache.avro.SchemaCompatibility.SchemaCompatibilityType
import vulcan.Codec

final class AvroSchemaCompatibilitySuite
    extends CatsEffectSuite
    with SchemaSuite
    with BookInstanceAddedToCatalogueAvroCodec:

  private[this] val kafkaConfig = KafkaTestConfig.LendingBookInstances.kafkaConfig

  private[this] lazy val checker = compatibilityChecker(
    SchemaRegistryClientSettings(kafkaConfig.schemaRegistry.value),
  )

  override def munitFixtures: Seq[Fixture[?]] = List(checker)

  test("book instance added to catalogue schema should be compatible".tag(online)) {
    checker()
      .checkReaderCompatibility(
        bookInstanceAddedToCatalogueAvroCodec,
        s"${kafkaConfig.topic}-value",
      )
      .map(compatibility =>
        assertEquals(
          compatibility.getType(),
          SchemaCompatibilityType.COMPATIBLE,
          compatibility.getResult().nn.getIncompatibilities(),
        ),
      )
  }
