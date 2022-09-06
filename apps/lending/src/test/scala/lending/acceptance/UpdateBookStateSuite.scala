package es.eriktorr.library
package lending.acceptance

import book.infrastructure.BookInstanceGenerators.bookIdGen
import lending.acceptance.UpdateBookStateSuite.{eventIdEntityDecoder, testCaseGen}
import lending.application.{PlaceHoldRequest, UpdateBookState}
import lending.infrastructure.LendingGenerators.{libraryBranchIdGen, patronIdGen}
import lending.model.{BookStateChanged, PatronId}
import shared.EventId
import shared.infrastructure.FakeEventPublisher.EventPublisherState
import shared.infrastructure.FakeUUIDGen.UUIDGenState
import shared.infrastructure.{EventIdJsonCodec, FakeEventPublisher, FakeUUIDGen, HttpServerSuite}
import shared.refined.types.infrastructure.RefinedTypesGenerators.eventIdGen

import cats.effect.std.UUIDGen
import cats.effect.{IO, Ref}
import org.http4s.circe.jsonOf
import org.http4s.{EntityDecoder, Status}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class UpdateBookStateSuite extends HttpServerSuite:

  test("should place a book on hold") {
    forAllF(testCaseGen) { testCase =>
      for
        eventPublisherStateRef <- Ref.of[IO, EventPublisherState[BookStateChanged]](
          EventPublisherState.empty,
        )
        uuidGeneratorStateRef <- Ref.of[IO, UUIDGenState](
          UUIDGenState.empty.set(List(testCase.eventId.get.value)),
        )
        httpApp = {
          given UUIDGen[IO] = FakeUUIDGen(uuidGeneratorStateRef)
          UpdateBookState(FakeEventPublisher(eventPublisherStateRef), ???).httpApp
        }
        request = requestFrom(s"patron/${testCase.patronId}/holds", testCase.request)
        _ <- checkUsing(httpApp, request, Status.Created, testCase.eventId)
      yield ()
    }
  }

object UpdateBookStateSuite extends EventIdJsonCodec:
  implicit val eventIdEntityDecoder: EntityDecoder[IO, EventId] = jsonOf[IO, EventId]

  final private case class TestCase(
      patronId: PatronId,
      request: PlaceHoldRequest,
      eventId: Option[EventId],
  )

  private[this] val placeHoldRequestGen = for
    bookId <- bookIdGen
    libraryBranchId <- libraryBranchIdGen
    numberOfDays <- Gen.choose(1, 7)
  yield PlaceHoldRequest(bookId, libraryBranchId, numberOfDays)

  private val testCaseGen = for
    patronId <- patronIdGen
    placeHoldRequest <- placeHoldRequestGen
    eventId <- eventIdGen
  yield TestCase(patronId, placeHoldRequest, Some(eventId))
