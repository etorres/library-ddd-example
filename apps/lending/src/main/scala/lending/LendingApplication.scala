package es.eriktorr.library
package lending

import lending.application.CreateAvailableBookOnInstanceAdded
import lending.infrastructure.{JdbcAvailableBooks, KafkaBookInstanceAddedToCatalogueEventHandler}

import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
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
            s"Running with parameters: ${parameters.asString}, and configuration: ${configuration.asString}",
          )
          _ <- program(configuration, parameters, logger)
          _ <- logger.info("Exiting!")
        yield ExitCode.Success
      case None => Console[IO].errorln(LendingParameters.usage).as(ExitCode.Error)

  private[this] def program(
      configuration: LendingConfiguration,
      parameters: LendingParameters,
      logger: Logger[IO],
  ): IO[Unit] =
    LendingResources.impl(configuration, runtime.compute).use {
      case LendingResources(bookInstanceAddedToCatalogueConsumer, jdbcTransactor) =>
        val availableBooks = JdbcAvailableBooks(jdbcTransactor)
        val bookInstanceAddedToCatalogueEventHandler =
          KafkaBookInstanceAddedToCatalogueEventHandler(bookInstanceAddedToCatalogueConsumer)
        val createAvailableBookOnInstanceAdded =
          CreateAvailableBookOnInstanceAdded(
            availableBooks,
            bookInstanceAddedToCatalogueEventHandler,
            parameters.libraryBranchId,
          )
        logger.info(
          s"Started library ${parameters.libraryBranchId}",
        ) *> createAvailableBookOnInstanceAdded.handle.compile.drain
    }
