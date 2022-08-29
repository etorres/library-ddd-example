package es.eriktorr.library
package shared.infrastructure

import cats.effect.IO
import fs2.Stream

trait EventHandler[A]:
  def handleWith(f: A => IO[Unit]): Stream[IO, Unit]
