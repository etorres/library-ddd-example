package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.FakeClock.ClockState

import cats.Applicative
import cats.effect.{Clock, IO, Ref}

import java.time.Instant
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

final class FakeClock(stateRef: Ref[IO, ClockState]) extends Clock[IO]:
  override def applicative: Applicative[IO] = Applicative[IO]

  override def monotonic: IO[FiniteDuration] = nextInstant

  override def realTime: IO[FiniteDuration] = nextInstant

  private[this] def nextInstant = stateRef.modify { currentState =>
    val (head, next) = currentState.instants match
      case ::(head, next) => (head, next)
      case Nil => throw new IllegalStateException("Instants exhausted")
    (currentState.copy(next), FiniteDuration(head.toEpochMilli, MILLISECONDS))
  }

object FakeClock:
  final case class ClockState(instants: List[Instant]):
    def set(newInstants: List[Instant]): ClockState = copy(instants = newInstants)

  object ClockState:
    def empty: ClockState = ClockState(List.empty)
