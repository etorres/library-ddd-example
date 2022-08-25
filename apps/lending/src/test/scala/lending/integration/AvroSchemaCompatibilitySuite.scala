package es.eriktorr.library
package lending.integration

import lending.infrastructure.BookInstanceAddedToCatalogueAvroCodec
import shared.infrastructure.KafkaConfig
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

  private[this] val checker = compatibilityChecker(
    SchemaRegistryClientSettings(KafkaConfig.default.schemaRegistry.value),
  )

  override def munitFixtures: Seq[Fixture[?]] = List(checker)

  test("book instance added to catalogue event schema should be compatible".tag(online)) {
    checker()
      .checkReaderCompatibility(
        bookInstanceAddedToCatalogueAvroCodec,
        s"${KafkaConfig.default.topic}-value",
      )
      .map(compatibility =>
        assertEquals(
          compatibility.getType(),
          SchemaCompatibilityType.COMPATIBLE,
          compatibility.getResult().nn.getIncompatibilities(),
        ),
      )
  }
