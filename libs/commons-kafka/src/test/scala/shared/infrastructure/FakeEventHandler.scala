package es.eriktorr.library
package shared.infrastructure

import shared.DomainEvent
import shared.infrastructure.FakeEventHandler.EventHandlerState

import cats.effect.{IO, Ref}
import fs2.Stream

final class FakeEventHandler[A <: DomainEvent](stateRef: Ref[IO, EventHandlerState[A]])
    extends EventHandler[A]:
  override def handleWith(f: A => IO[Unit]): Stream[IO, Unit] =
    Stream.exec(
      for
        headEvent <- stateRef
          .getAndUpdate { currentState =>
            currentState.events match
              case ::(_, next) => currentState.copy(events = next)
              case Nil => currentState
          }
          .map(_.events.headOption)
        _ <- headEvent.fold(IO.unit)(f.apply)
      yield (),
    )

object FakeEventHandler:
  final case class EventHandlerState[A](events: List[A]):
    def set(newEvents: List[A]): EventHandlerState[A] = copy(events = newEvents)

  object EventHandlerState:
    def empty[A]: EventHandlerState[A] = EventHandlerState(List.empty)
