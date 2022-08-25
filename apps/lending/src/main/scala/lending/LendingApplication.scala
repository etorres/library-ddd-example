package es.eriktorr.library
package lending

import book.model.BookInstanceAddedToCatalogue
import shared.infrastructure.KafkaClients
import shared.infrastructure.KafkaClients.KafkaConsumerIO

import cats.effect.*
import cats.effect.std.Console
import cats.effect.syntax.all.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LendingApplication extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    LendingParameters.from(args) match
      case Some(parameters) =>
        for
          logger <- Slf4jLogger.create[IO]
          configuration <- LendingConfiguration.load
          _ <- logger.info(
            s"Running with configuration: ${configuration.asString} and parameters: ${parameters.asString}",
          )
          _ <- program(configuration, parameters, logger)
          _ <- logger.info("Exiting!")
        yield ExitCode.Success
      case None => Console[IO].errorln(LendingParameters.usage).as(ExitCode.Error)

  private[this] def program(
      configuration: LendingConfiguration,
      parameters: LendingParameters,
      logger: Logger[IO],
  ): IO[Unit] = LendingResources.impl(configuration, runtime.compute).use {
    case LendingResources(kafkaConsumer, jdbcTransactor) => kk(kafkaConsumer, logger)
  }

  private[this] def kk(kafkaConsumer: KafkaConsumerIO[BookInstanceAddedToCatalogue], logger: Logger[IO]): IO[Unit] = for
    stoppedDeferred <- Deferred[IO, Either[Throwable, Unit]]
    gracefulShutdownStartedRef <- Ref[IO].of(false)
    _ <- kafkaConsumer
      .bracketCase { case (consumer, _) =>
        handleEventsWith(consumer, logger).attempt.flatMap { result =>
          gracefulShutdownStartedRef.get.flatMap {
            case true => stoppedDeferred.complete(result)
            case false => IO.pure(result).rethrow
          }
        }
      } { case ((consumer, closeConsumer), exitCase) =>
        (exitCase match
          case Outcome.Errored(e) => Console[IO].errorln(s"Error caught: ${e.getMessage}")
          case _ =>
            for
              _ <- gracefulShutdownStartedRef.set(true)
              _ <- consumer.stopConsuming
              stopResult <- stoppedDeferred.get.timeoutTo(
                10.seconds,
                IO.pure(Left(new RuntimeException("Graceful shutdown timed out"))),
              )
              _ <- stopResult match
                case Right(()) => IO.unit
                case Left(e) => Console[IO].errorln(s"Error caught: ${e.getMessage}")
            yield ()
        ).guarantee(closeConsumer)
      }
  yield ()

  private[this] def handleEventsWith(
      consumer: KafkaConsumerIO[BookInstanceAddedToCatalogue],
      logger: Logger[IO],
  ): IO[Unit] = ???
