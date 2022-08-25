package es.eriktorr.library
package shared.infrastructure

import munit.Assertions.assertEquals
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Prop}
import vulcan.Codec

object AvroRoundTripSuite:

  def checkUsing[A](genA: Gen[A], codecA: Codec[A])(implicit ev: Unit => Prop): Prop =
    forAll(genA) { a =>
      assertEquals(
        for
          schema <- codecA.schema
          payload <- codecA.encode(a)
          result <- codecA.decode(payload, schema)
        yield result,
        Right(a),
      )
    }
