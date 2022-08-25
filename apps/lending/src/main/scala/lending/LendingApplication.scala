package es.eriktorr.library
package lending

import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LendingApplication extends IOApp:
  private[this] def program(
      configuration: LendingConfiguration,
      parameters: LendingParameters,
  ): IO[Unit] = ???

  override def run(args: List[String]): IO[ExitCode] =
    LendingParameters.parametersFrom(args) match
      case Some(parameters) =>
        for
          logger <- Slf4jLogger.create[IO]
          configuration <- LendingConfiguration.load
          _ <- logger.info(
            s"Running with configuration: ${configuration.asString} and parameters: ${parameters.asString}",
          )
          _ <- program(configuration, parameters)
          _ <- logger.info("Exiting!")
        yield ExitCode.Success
      case None => Console[IO].errorln(LendingParameters.usage).as(ExitCode.Error)
