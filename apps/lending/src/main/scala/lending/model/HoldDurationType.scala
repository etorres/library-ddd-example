package es.eriktorr.library
package lending.model

import java.time.Instant
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

enum HoldDurationType[+A <: Instant, +B <: Duration]:
  case OpenEnded(a: A) extends HoldDurationType[A, Nothing]
  case CloseEnded(a: A, val duration: B) extends HoldDurationType[A, B]

  def from: Instant = this match
    case OpenEnded(a) => a
    case CloseEnded(a, _) => a

  def to: Option[Instant] = this match
    case OpenEnded(_) => None
    case CloseEnded(a, b) => Some(a.plusMillis(b.toMillis).nn)

object HoldDurationType:
  type HoldDuration = HoldDurationType[Instant, Duration]

  object HoldDuration:
    def from(when: Instant, numberOfDays: Option[Int]): HoldDuration =
      numberOfDays
        .map(days => CloseEnded(when, Duration(days, TimeUnit.DAYS)))
        .getOrElse(OpenEnded(when))
