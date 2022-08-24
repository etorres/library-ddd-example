package es.eriktorr.library
package infrastructure

import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Prop}
import vulcan.Codec

object AvroRoundTripSuite extends ScalaCheckSuite:

  def checkUsing[A](genA: Gen[A], codecA: Codec[A]): Prop = forAll(genA) { a =>
    assertEquals(
      for
        schema <- codecA.schema
        payload <- codecA.encode(a)
        result <- codecA.decode(payload, schema)
      yield result,
      Right(a),
    )
  }
