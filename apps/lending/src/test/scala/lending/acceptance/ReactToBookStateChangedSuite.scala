package es.eriktorr.library
package lending.acceptance

import lending.acceptance.ReactToBookStateChangedSuite.availableBookIsPlacedOnHold
import lending.infrastructure.LendingGenerators.bookPlacedOnHoldGen
import lending.infrastructure.ReactToBookStateChangedSuiteRunner
import lending.infrastructure.ReactToBookStateChangedSuiteRunner.ReactToBookStateChangedState
import lending.model.Book.{AvailableBook, BookOnHold}

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class ReactToBookStateChangedSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  test("should place an available book on hold") {
    forAllF(availableBookIsPlacedOnHold) { testCase =>
      ReactToBookStateChangedSuiteRunner
        .runWith(testCase.initialState)(_.handle.compile.drain)
        .map { case (result, finalState) =>
          // TODO
          println(s"\n\n >> FINAL    : $finalState\n")
          println(s"\n\n >> EXPECTED : ${testCase.expectedState}\n")
          // TODO
          assert(result.isRight)
          assertEquals(finalState, testCase.expectedState)
        }
    }
  }

object ReactToBookStateChangedSuite:
  final private case class TestCase(
      initialState: ReactToBookStateChangedState,
      expectedState: ReactToBookStateChangedState,
  )

  final private val availableBookIsPlacedOnHold: Gen[TestCase] = for
    bookPlacedOnHold <- bookPlacedOnHoldGen
    initialState = ReactToBookStateChangedState.empty
      .setEvents(List(bookPlacedOnHold))
      .setBooks(
        List(
          AvailableBook(
            bookPlacedOnHold.bookId,
            bookPlacedOnHold.bookType,
            bookPlacedOnHold.libraryBranchId,
          ),
        ),
      )
    expectedState = initialState.clearEvents.setBooks(List(BookOnHold.from(bookPlacedOnHold)))
  yield TestCase(initialState, expectedState)
