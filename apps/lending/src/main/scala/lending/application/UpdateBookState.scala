package es.eriktorr.library
package lending.application

import lending.application.UpdateBookState.{
  eventIdJsonEncoder,
  placeHoldRequestEntityDecoder,
  ApiRootPath,
  PatronIdVar,
}
import lending.model.*
import lending.model.Book.AvailableBook
import lending.model.BookStateChanged.BookPlacedOnHold
import lending.model.HoldDurationType.HoldDuration
import lending.model.Patron.PatronHoldsAndOverdueCheckouts
import lending.model.PlacingOnHoldPolicy.PlacingOnHoldDecisionType.Allowance
import shared.EventId
import shared.RESTful.{apiPathPrefix, apiVersion}
import shared.infrastructure.{EventIdJsonCodec, EventPublisher}

import cats.effect.{Clock, IO}
import cats.effect.std.UUIDGen
import cats.syntax.all.*
import io.circe.syntax.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.{EntityDecoder, HttpApp, HttpRoutes, Request}

final class UpdateBookState(
    availableBooks: AvailableBooks,
    bookStateChangedEventPublisher: EventPublisher[BookStateChanged],
    patrons: Patrons,
    placingOnHoldPolicies: PlacingOnHoldPolicies,
)(using clock: Clock[IO], uuidGenerator: UUIDGen[IO]):
  val httpApp: HttpApp[IO] = HttpRoutes
    .of[IO] { case request @ POST -> ApiRootPath / "patron" / PatronIdVar(patronId) / "holds" =>
      placeHold(patronId, request)
    }
    .orNotFound

  private[this] def placeHold(patronId: PatronId, request: Request[IO]) = for
    placeHoldRequest <- request.as[PlaceHoldRequest]
    availableBook <- availableBooks.findAvailableBookBy(placeHoldRequest.bookId)
    patronHoldsAndOverdueCheckouts <- patrons.findBy(patronId)
    response <- (availableBook, patronHoldsAndOverdueCheckouts).mapN((_, _)).fold(NotFound()) {
      case (availableBook, patronHoldsAndOverdueCheckouts) =>
        for
          when <- clock.realTimeInstant
          holdDuration = HoldDuration.from(when, placeHoldRequest.numberOfDays)
          allCurrentPolicies <- placingOnHoldPolicies.allCurrentPolicies
          rejections = allCurrentPolicies
            .filter(
              _.canHold(availableBook, patronHoldsAndOverdueCheckouts, holdDuration) != Allowance,
            )
          response <-
            if rejections.isEmpty then
              for
                eventId <- uuidGenerator.randomUUID.map(EventId.from)
                bookPlacedOnHold = BookPlacedOnHold(
                  eventId,
                  when,
                  patronId,
                  availableBook.bookId,
                  availableBook.bookType,
                  availableBook.libraryBranchId,
                  holdDuration.from,
                  holdDuration.to,
                )
                _ <- patrons.save(bookPlacedOnHold)
                _ <- bookStateChangedEventPublisher.publish(bookPlacedOnHold)
                // TODO: check and publish MaximumNumberOhHoldsReached
                response <- Created(eventId.asJson.noSpaces)
              yield response
            else
              BadRequest(
                "",
              ) // TODO: 1) publish BookHoldFailed; and 2) add rejections to bad request response
        yield response
    }
  yield response

object UpdateBookState extends EventIdJsonCodec:
  val ApiRootPath: Path = Root / apiPathPrefix / apiVersion

  private object PatronIdVar:
    def unapply(str: String): Option[PatronId] = PatronId.from(str).toOption

  implicit val placeHoldRequestEntityDecoder: EntityDecoder[IO, PlaceHoldRequest] =
    jsonOf[IO, PlaceHoldRequest]
