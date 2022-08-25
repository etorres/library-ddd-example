package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookGenerators.bookInstanceGen
import lending.model.BookInstanceAddedToCatalogue
import shared.refined.types.infrastructure.RefinedTypesGenerators.uuidGen

import org.scalacheck.{Arbitrary, Gen}

import java.time.Instant

object LendingGenerators:
  /** Generates an instant to a precision of milliseconds.
    */
  val instantArbitrary: Arbitrary[Instant] =
    Arbitrary(Gen.posNum[Long].map { millis =>
      val instant = Instant.ofEpochMilli(millis).nn
      instant.minusNanos(instant.getNano().toLong).nn
    })

  val bookInstanceAddedToCatalogueGen: Gen[BookInstanceAddedToCatalogue] = for
    eventId <- uuidGen
    when <- instantArbitrary.arbitrary
    bookInstance <- bookInstanceGen()
  yield BookInstanceAddedToCatalogue(eventId, when, bookInstance)
