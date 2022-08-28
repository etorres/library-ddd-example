package es.eriktorr.library
package catalogue.infrastructure

import shared.infrastructure.EventPublisher

import cats.effect.{IO, Ref}

final case class EventPublisherState[A](events: List[A])

object EventPublisherState:
  def empty[A]: EventPublisherState[A] = EventPublisherState(List.empty)

final class FakeEventPublisher[A](stateRef: Ref[IO, EventPublisherState[A]])
    extends EventPublisher[A]:
  override def publish(event: A): IO[Unit] = ???
