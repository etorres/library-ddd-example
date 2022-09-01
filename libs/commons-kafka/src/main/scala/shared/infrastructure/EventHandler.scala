package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaClients.KafkaConsumerIO

import cats.effect.IO
import fs2.Stream
import fs2.kafka.commitBatchWithin

import scala.concurrent.duration.*

trait EventHandler[A]:
  def handleWith(f: A => IO[Unit]): Stream[IO, Unit]

object EventHandler:
  abstract class CommittableEventHandler[A](consumer: KafkaConsumerIO[A]) extends EventHandler[A]:
    override def handleWith(f: A => IO[Unit]): Stream[IO, Unit] =
      consumer.stream
        .mapAsync(16) { committable =>
          val event = committable.record.value
          f.apply(event).as(committable.offset)
        }
        .through(commitBatchWithin(100, 15.seconds))
