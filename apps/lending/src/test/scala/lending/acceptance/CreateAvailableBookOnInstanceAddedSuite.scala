package es.eriktorr.library
package lending.acceptance

import book.infrastructure.BookGenerators.bookInstanceGen
import book.model.BookInstanceAddedToCatalogue
import lending.acceptance.CreateAvailableBookOnInstanceAddedSuite.testCaseGen
import lending.infrastructure.CreateAvailableBookOnInstanceAddedSuiteRunner
import lending.infrastructure.CreateAvailableBookOnInstanceAddedSuiteRunner.CreateAvailableBookOnInstanceAddedState
import lending.model.AvailableBook
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.uuidGen

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF

import scala.concurrent.duration.*

final class CreateAvailableBookOnInstanceAddedSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:

  test("should create an available book when a new books instance is added") {
    forAllF(testCaseGen) { testCase =>
      CreateAvailableBookOnInstanceAddedSuiteRunner
        .runWith(testCase.initialState)(
          _.handle.timeout(30.seconds).take(1).compile.drain,
        )
        .map { case (result, finalState) =>
          assert(result.isRight)
          assertEquals(finalState, testCase.expectedState)
        }
    }
  }

object CreateAvailableBookOnInstanceAddedSuite:
  final private case class TestCase(
      initialState: CreateAvailableBookOnInstanceAddedState,
      expectedState: CreateAvailableBookOnInstanceAddedState,
  )

  final private val testCaseGen = for
    bookInstance <- bookInstanceGen()
    eventId <- uuidGen
    when <- instantArbitrary.arbitrary
    libraryBranchId <- uuidGen
    initialState = CreateAvailableBookOnInstanceAddedState
      .from(libraryBranchId)
      .setEvents(List(BookInstanceAddedToCatalogue(eventId, when, bookInstance)))
    expectedState = initialState.clearEvents.setAvailableBooks(
      List(AvailableBook.from(bookInstance, libraryBranchId)),
    )
  yield TestCase(initialState, expectedState)
