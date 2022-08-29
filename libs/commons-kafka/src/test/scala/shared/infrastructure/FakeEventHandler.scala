package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.FakeEventHandler.EventHandlerState

import cats.effect.{IO, Ref}
import fs2.Stream

final class FakeEventHandler[A](stateRef: Ref[IO, EventHandlerState[A]]) extends EventHandler[A]:
  override def handleWith(f: A => IO[Unit]): Stream[IO, Unit] =
    Stream.eval(stateRef.update { currentState =>
      currentState.events match
        case ::(head, next) =>
          f.apply(head)
          currentState.copy(events = next)
        case Nil => currentState
    })

object FakeEventHandler:
  final case class EventHandlerState[A](events: List[A]):
    def set(newEvents: List[A]): EventHandlerState[A] = copy(events = newEvents)

  object EventHandlerState:
    def empty[A]: EventHandlerState[A] = EventHandlerState(List.empty)
