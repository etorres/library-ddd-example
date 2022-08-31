package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.FakeUUIDGen.UUIDGenState

import cats.effect.std.UUIDGen
import cats.effect.{IO, Ref}

import java.util.UUID

final class FakeUUIDGen(stateRef: Ref[IO, UUIDGenState]) extends UUIDGen[IO]:
  override def randomUUID: IO[UUID] = stateRef.modify { currentState =>
    val (head, next) = currentState.uuids match
      case ::(head, next) => (head, next)
      case Nil => throw new IllegalStateException("UUIDs exhausted")
    (currentState.copy(next), head)
  }

object FakeUUIDGen:
  final case class UUIDGenState(uuids: List[UUID]):
    def set(newUuids: List[UUID]): UUIDGenState = copy(uuids = newUuids)

  object UUIDGenState:
    def empty: UUIDGenState = UUIDGenState(List.empty)
