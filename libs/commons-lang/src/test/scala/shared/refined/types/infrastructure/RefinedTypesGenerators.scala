package es.eriktorr.library
package shared.refined.types.infrastructure

import shared.EventId
import shared.refined.types.NonEmptyString

import org.scalacheck.Gen

object RefinedTypesGenerators:
  val nonEmptyStringGen: Gen[NonEmptyString] =
    Gen.nonEmptyListOf(Gen.alphaNumChar).map(xs => NonEmptyString.unsafeFrom(xs.mkString("")))

  val eventIdGen: Gen[EventId] = Gen.uuid.map(EventId.from)
