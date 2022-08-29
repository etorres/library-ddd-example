package es.eriktorr.library
package shared.infrastructure

import org.scalacheck.{Arbitrary, Gen}

import java.time.Instant

object TimeGenerators:
  /** Generates an instant to a precision of milliseconds.
    */
  val instantArbitrary: Arbitrary[Instant] =
    Arbitrary(
      Gen.choose(Instant.EPOCH.nn.toEpochMilli, Instant.now.nn.toEpochMilli).map { millis =>
        val instant = Instant.ofEpochMilli(millis).nn
        instant.minusNanos(instant.getNano.toLong).nn
      },
    )
