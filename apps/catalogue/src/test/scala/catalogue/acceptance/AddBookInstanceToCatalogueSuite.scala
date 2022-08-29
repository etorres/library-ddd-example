package es.eriktorr.library
package catalogue.acceptance

import book.infrastructure.BookGenerators.{bookGen, bookInstanceGen}
import book.model.{BookInstance, BookInstanceAddedToCatalogue}
import catalogue.acceptance.AddBookInstanceToCatalogueSuite.testCaseGen
import catalogue.application.AddBookInstanceToCatalogue
import catalogue.infrastructure.{AddBookInstanceToCatalogueRunner, AddBookInstanceToCatalogueState}
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.uuidGen

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF

final class AddBookInstanceToCatalogueSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  test("should add a book instance to the catalogue") {
    forAllF(testCaseGen) { testCase =>
      AddBookInstanceToCatalogueRunner
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

  final private val testCaseGen = for
    book <- bookGen
    bookInstance <- bookInstanceGen(book.isbn)
    eventId <- uuidGen
    when <- instantArbitrary.arbitrary
    initialState = AddBookInstanceToCatalogueState.empty
      .setBooks(Map(book -> List.empty))
      .setInstants(List(when))
      .setUUIDs(List(eventId))
    expectedState = initialState.clearInstants.clearUUIDs
      .setBooks(Map(book -> List(bookInstance)))
      .setEventPublisherState(List(BookInstanceAddedToCatalogue(eventId, when, bookInstance)))
  yield TestCase(bookInstance, initialState, expectedState)
