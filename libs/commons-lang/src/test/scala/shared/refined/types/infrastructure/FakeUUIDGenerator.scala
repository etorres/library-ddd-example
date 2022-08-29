package es.eriktorr.library
package shared.refined.types.infrastructure

import shared.refined.types.UUID
import shared.refined.types.infrastructure.FakeUUIDGenerator.UUIDGeneratorState

import cats.effect.{IO, Ref}

final class FakeUUIDGenerator(stateRef: Ref[IO, UUIDGeneratorState]) extends UUIDGenerator[IO]:
  override def randomUUID: IO[UUID] = stateRef.modify { currentState =>
    val (head, next) = currentState.uuids match
      case ::(head, next) => (head, next)
      case Nil => throw new IllegalStateException("UUIDs exhausted")
    (currentState.copy(next), head)
  }

object FakeUUIDGenerator:
  final case class UUIDGeneratorState(uuids: List[UUID]):
    def set(newUuids: List[UUID]): UUIDGeneratorState = copy(uuids = newUuids)

  object UUIDGeneratorState:
    def empty: UUIDGeneratorState = UUIDGeneratorState(List.empty)
