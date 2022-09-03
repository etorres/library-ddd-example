package es.eriktorr.library
package lending.acceptance

import book.infrastructure.BookInstanceGenerators.bookInstanceAddedToCatalogueGen
import book.model.BookInstanceAddedToCatalogue
import lending.acceptance.CreateAvailableBookOnInstanceAddedSuite.testCaseGen
import lending.infrastructure.CreateAvailableBookOnInstanceAddedSuiteRunner
import lending.infrastructure.CreateAvailableBookOnInstanceAddedSuiteRunner.CreateAvailableBookOnInstanceAddedState
import lending.infrastructure.LendingGenerators.libraryBranchIdGen
import lending.model.Book.AvailableBook

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF

import scala.concurrent.duration.*

final class CreateAvailableBookOnInstanceAddedSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite:

  test("should create new available book for lending when book instance was added to catalogue") {
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
    bookInstanceAddedToCatalogue <- bookInstanceAddedToCatalogueGen
    libraryBranchId <- libraryBranchIdGen
    initialState = CreateAvailableBookOnInstanceAddedState
      .from(libraryBranchId)
      .setEvents(List(bookInstanceAddedToCatalogue))
    expectedState = initialState.clearEvents.setBooks(
      List(AvailableBook.from(bookInstanceAddedToCatalogue, libraryBranchId)),
    )
  yield TestCase(initialState, expectedState)
