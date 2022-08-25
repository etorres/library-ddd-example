package es.eriktorr.library
package shared.refined.types.infrastructure

import shared.refined.types.{NonEmptyString, UUID}

import org.scalacheck.Gen

object RefinedTypesGenerators:
  val nonEmptyStringGen: Gen[NonEmptyString] =
    Gen.nonEmptyListOf(Gen.alphaNumChar).map(xs => NonEmptyString.unsafeFrom(xs.mkString("")))

  val uuidGen: Gen[UUID] = Gen.uuid.map(x => UUID.unsafeFrom(x.toString))
