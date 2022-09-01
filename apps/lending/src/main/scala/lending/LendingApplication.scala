package es.eriktorr.library
package lending

import lending.application.{CreateAvailableBookOnInstanceAdded, ReactToChangeOnBookState}
import lending.infrastructure.KafkaBookStateChangedEvenHandlers.*
import lending.infrastructure.{
  JdbcBooks,
  KafkaBookDuplicateHoldFoundEventPublisher,
  KafkaBookInstanceAddedToCatalogueEventHandler,
}

import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.parallel.*
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
      case LendingResources(
            bookInstanceAddedToCatalogueConsumer,
            bookPlacedOnHoldConsumer,
            bookCheckedOutConsumer,
            bookHoldExpiredConsumer,
            bookHoldCanceledConsumer,
            bookReturnedConsumer,
            bookDuplicateHoldFoundProducer,
            jdbcTransactor,
          ) =>
        val books = JdbcBooks(jdbcTransactor)

        val createAvailableBookOnInstanceAdded =
          CreateAvailableBookOnInstanceAdded(
            books,
            KafkaBookInstanceAddedToCatalogueEventHandler(bookInstanceAddedToCatalogueConsumer),
            parameters.libraryBranchId,
          )

        val reactToChangeOnBookState = ReactToChangeOnBookState(
          books,
          KakfaBookPlacedOnHoldEventHandler(bookPlacedOnHoldConsumer),
          KakfaBookCheckedOutEventHandler(bookCheckedOutConsumer),
          KakfaBookHoldExpiredEventHandler(bookHoldExpiredConsumer),
          KakfaBookHoldCanceledEventHandler(bookHoldCanceledConsumer),
          KakfaBookReturnedEventHandler(bookReturnedConsumer),
          KafkaBookDuplicateHoldFoundEventPublisher(
            bookDuplicateHoldFoundProducer,
            configuration.kafkaConfig.topic,
            logger,
          ),
        )

        logger.info(
          s"Started library ${parameters.libraryBranchId}",
        ) *> (
          createAvailableBookOnInstanceAdded.handle.compile.drain,
          reactToChangeOnBookState.handleBookPlacedOnHold.compile.drain,
          reactToChangeOnBookState.handleBookCheckedOut.compile.drain,
          reactToChangeOnBookState.handleBookHoldExpired.compile.drain,
          reactToChangeOnBookState.handleBookHoldCanceled.compile.drain,
          reactToChangeOnBookState.handleBookReturned.compile.drain,
        ).parMapN((_, _, _, _, _, _) => ())
    }
