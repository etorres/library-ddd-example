package es.eriktorr.library
package shared.infrastructure

import cats.effect.IO

trait EventPublisher[A]:
  def publish(event: A): IO[Unit]
