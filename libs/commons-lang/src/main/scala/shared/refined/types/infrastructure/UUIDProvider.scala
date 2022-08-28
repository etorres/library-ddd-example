package es.eriktorr.library
package shared.refined.types.infrastructure

import shared.refined.types.UUID

import cats.effect.IO
import cats.effect.std.UUIDGen

trait UUIDProvider:
  def randomUUID: IO[UUID]

object UUIDProvider:
  given UUIDProvider = new UUIDProvider():
    override def randomUUID: IO[UUID] = UUIDGen.randomUUID[IO].map(UUID.fromJava)
