package es.eriktorr.library
package catalogue

import catalogue.application.AddBookInstanceToCatalogue
import catalogue.infrastructure.{JdbcCatalogue, KafkaBookInstanceAddedToCatalogueEventPublisher}

import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object CatalogueApplication extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    CatalogueParameters.from(args) match
      case Some(parameters) =>
        for
          logger <- Slf4jLogger.create[IO]
          configuration <- CatalogueConfiguration.load
          _ <- logger.info(
            s"Running with parameters: ${parameters.asString}, and configuration: ${configuration.asString}",
          )
          _ <- program(configuration, parameters, logger)
          _ <- logger.info("Exiting!")
        yield ExitCode.Success
      case None => Console[IO].errorln(CatalogueParameters.usage).as(ExitCode.Error)

  private[this] def program(
      configuration: CatalogueConfiguration,
      parameters: CatalogueParameters,
      logger: Logger[IO],
  ): IO[Unit] =
    CatalogueResources.impl(configuration, runtime.compute).use {
      case CatalogueResources(bookInstanceAddedToCatalogueProducer, jdbcTransactor) =>
        val catalogue = JdbcCatalogue(jdbcTransactor)
        val bookInstanceAddedToCatalogueEventPublisher =
          KafkaBookInstanceAddedToCatalogueEventPublisher(
            bookInstanceAddedToCatalogueProducer,
            configuration.bookInstancesKafkaConfig.topic,
            logger,
          )
        val addBookInstanceToCatalogue =
          AddBookInstanceToCatalogue(catalogue, bookInstanceAddedToCatalogueEventPublisher)
        // TODO
        IO.unit
    }
