package es.eriktorr.library
package catalogue.acceptance

import book.infrastructure.BookGenerators.{bookGen, bookInstanceGen}
import book.model.{BookInstance, BookInstanceAddedToCatalogue}
import catalogue.acceptance.AddBookInstanceToCatalogueSuite.{noBookWithIsbn, thereIsABookWithIsbn}
import catalogue.application.AddBookInstanceToCatalogue
import catalogue.infrastructure.AddBookInstanceToCatalogueSuiteRunner
import catalogue.infrastructure.AddBookInstanceToCatalogueSuiteRunner.AddBookInstanceToCatalogueState
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.uuidGen

import cats.syntax.either.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.effect.PropF.forAllF

final class AddBookInstanceToCatalogueSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  test("should add a new book instance to the catalogue") {
    forAllF(thereIsABookWithIsbn) { testCase =>
      AddBookInstanceToCatalogueSuiteRunner
        .runWith(testCase.initialState)(_.add(testCase.bookInstance))
        .map { case (result, finalState) =>
          assert(result.isRight)
          assertEquals(finalState, testCase.expectedState)
        }
    }
  }

  test("should not publish any event when adding a new book instance if catalogue fails") {
    forAllF(noBookWithIsbn) { testCase =>
      AddBookInstanceToCatalogueSuiteRunner
        .runWith(testCase.initialState)(_.add(testCase.bookInstance))
        .map { case (result, finalState) =>
          assert(result.isLeft)
          assertEquals(
            result.leftMap(_.getMessage),
            Left(s"There is no book with ISBN: ${testCase.bookInstance.isbn}"),
          )
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

  final private val thereIsABookWithIsbn = for
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
      .setEvents(List(BookInstanceAddedToCatalogue(eventId, when, bookInstance)))
  yield TestCase(bookInstance, initialState, expectedState)

  final private val noBookWithIsbn = for
    bookInstance <- bookInstanceGen()
    initialState = AddBookInstanceToCatalogueState.empty
    expectedState = initialState
  yield TestCase(bookInstance, initialState, expectedState)
