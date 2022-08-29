package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.EventPublisher
import shared.infrastructure.FakeEventPublisher.EventPublisherState

import cats.effect.{IO, Ref}

final class FakeEventPublisher[A](stateRef: Ref[IO, EventPublisherState[A]])
    extends EventPublisher[A]:
  override def publish(event: A): IO[Unit] =
    stateRef.update(currentState => currentState.copy(event :: currentState.events))

object FakeEventPublisher:
  final case class EventPublisherState[A](events: List[A]):
    def set(newEvents: List[A]): EventPublisherState[A] = copy(events = newEvents)

  object EventPublisherState:
    def empty[A]: EventPublisherState[A] = EventPublisherState(List.empty)
