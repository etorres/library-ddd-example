package es.eriktorr.library
package lending.application

import lending.application.UpdateBookState.{
  eventIdJsonEncoder,
  placeHoldRequestEntityDecoder,
  ApiRootPath,
  PatronIdVar,
}
import lending.model.{Books, BookStateChanged, PatronId}
import shared.EventId
import shared.RESTful.{apiPathPrefix, apiVersion}
import shared.infrastructure.{EventIdJsonCodec, EventPublisher}

import cats.effect.IO
import cats.effect.std.UUIDGen
import io.circe.syntax.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.{EntityDecoder, HttpApp, HttpRoutes, Request}

final class UpdateBookState(eventSender: EventPublisher[BookStateChanged])(using
    uuidGenerator: UUIDGen[IO],
):
  val httpApp: HttpApp[IO] = HttpRoutes
    .of[IO] { case request @ POST -> ApiRootPath / "patron" / PatronIdVar(patronId) / "holds" =>
      placeHold(patronId, request)
    }
    .orNotFound

  private[this] def placeHold(patronId: PatronId, request: Request[IO]) = for
    placeHoldRequest <- request.as[PlaceHoldRequest]
    eventId <- uuidGenerator.randomUUID.map(EventId.from)
    response <- Created(eventId.asJson.noSpaces)
  yield response

object UpdateBookState extends EventIdJsonCodec:
  val ApiRootPath: Path = Root / apiPathPrefix / apiVersion

  private object PatronIdVar:
    def unapply(str: String): Option[PatronId] = PatronId.from(str).toOption

  implicit val placeHoldRequestEntityDecoder: EntityDecoder[IO, PlaceHoldRequest] =
    jsonOf[IO, PlaceHoldRequest]
