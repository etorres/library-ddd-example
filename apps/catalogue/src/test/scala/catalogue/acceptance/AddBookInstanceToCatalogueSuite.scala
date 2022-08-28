package es.eriktorr.library
package catalogue.acceptance

import book.infrastructure.BookGenerators.bookInstanceGen
import book.model.BookInstance
import catalogue.acceptance.AddBookInstanceToCatalogueSuite.testCaseGen
import catalogue.application.AddBookInstanceToCatalogue
import catalogue.infrastructure.{AddBookInstanceToCatalogueState, FakeAddBookInstanceToCatalogue}

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF

final class AddBookInstanceToCatalogueSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  test("should add a book instance to the catalogue") {
    forAllF(testCaseGen) { testCase =>
      FakeAddBookInstanceToCatalogue
        .withState(testCase.initialState)(_.add(testCase.bookInstance))
        .map { case (result, finalState) =>
          assert(result.isRight)
          assertEquals(finalState, testCase.expectedState)
        }
    }
  }

object AddBookInstanceToCatalogueSuite:
  final private case class TestCase(
      bookInstance: BookInstance,
      initialState: AddBookInstanceToCatalogueState,
      expectedState: AddBookInstanceToCatalogueState,
  )

  final private val testCaseGen =
    for bookInstance <- bookInstanceGen
    yield TestCase(???, ???, ???)
