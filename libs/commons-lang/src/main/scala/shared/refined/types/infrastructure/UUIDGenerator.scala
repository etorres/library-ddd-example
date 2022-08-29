package es.eriktorr.library
package shared.refined.types.infrastructure

import shared.refined.types.UUID

import cats.effect.IO
import cats.effect.std.UUIDGen

trait UUIDGenerator[F[_]]:
  def randomUUID: F[UUID]

object UUIDGenerator:
  given UUIDGenerator[IO] = new UUIDGenerator[IO]():
    override def randomUUID: IO[UUID] = UUIDGen.randomUUID[IO].map(UUID.fromJava)
