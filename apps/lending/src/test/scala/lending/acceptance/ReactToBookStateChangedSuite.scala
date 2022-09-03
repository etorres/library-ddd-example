package es.eriktorr.library
package lending.acceptance

import book.model.BookType
import lending.acceptance.ReactToBookStateChangedSuite.*
import lending.infrastructure.LendingGenerators.*
import lending.infrastructure.ReactToBookStateChangedSuiteRunner
import lending.infrastructure.ReactToBookStateChangedSuiteRunner.ReactToBookStateChangedState
import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import lending.model.BookDuplicateHoldFound
import shared.infrastructure.CollectionGenerators.nDistinct
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.eventIdGen

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class ReactToBookStateChangedSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  test("should checked out a book on hold") {
    checkUsing(bookOnHoldIsCheckedOut)
  }

  test("should make available a book on hold by canceling the hold") {
    checkUsing(bookHoldIsCanceled)
  }

  test("should make available a book on hold when the hold expires") {
    checkUsing(bookHoldExpired)
  }

  test("should place an available book on hold") {
    checkUsing(availableBookIsPlacedOnHold)
  }

  test(
    "should do nothing when a patron try to place on hold a book placed on hold already by themself",
  ) {
    checkUsing(bookPlacedOnHoldAlreadyBySamePatron)
  }

  test(
    "should raise a duplicate hold error when a different patron has the book placed on hold already",
  ) {
    checkUsing(bookPlacedOnHoldAlreadyByOtherPatron)
  }

  test("should make available a book on hold when the book is returned") {
    checkUsing(bookOnHoldIsReturned)
  }

  test("should do nothing when the state change is illegal") {
    fail("not implemented")
  }

  private[this] def checkUsing(testCaseGen: Gen[TestCase]) = forAllF(testCaseGen) { testCase =>
    ReactToBookStateChangedSuiteRunner
      .runWith(testCase.initialState)(_.handle.compile.drain)
      .map { case (result, finalState) =>
        assert(result.isRight)
        assertEquals(finalState, testCase.expectedState)
      }
  }

object ReactToBookStateChangedSuite:
  final private case class TestCase(
      initialState: ReactToBookStateChangedState,
      expectedState: ReactToBookStateChangedState,
  )

  final private val bookOnHoldIsCheckedOut: Gen[TestCase] = for
    bookCheckedOut <- bookCheckedOutGen
    holdTill <- Gen.option(instantArbitrary.arbitrary)
    bookOnHold = BookOnHold(
      bookCheckedOut.bookId,
      bookCheckedOut.bookType,
      bookCheckedOut.libraryBranchId,
      bookCheckedOut.patronId,
      holdTill,
    )
    initialState = ReactToBookStateChangedState.empty
      .setEvents(List(bookCheckedOut))
      .setBooks(List(bookOnHold))
    expectedState = initialState.clearEvents.setBooks(List(CheckedOutBook.from(bookCheckedOut)))
  yield TestCase(initialState, expectedState)

  final private val bookHoldIsCanceled: Gen[TestCase] = for
    bookHoldCanceled <- bookHoldCanceledGen
    bookType <- Gen.oneOf(BookType.values.toList)
    holdTill <- Gen.option(instantArbitrary.arbitrary)
    bookOnHold = BookOnHold(
      bookHoldCanceled.bookId,
      bookType,
      bookHoldCanceled.libraryBranchId,
      bookHoldCanceled.patronId,
      holdTill,
    )
    initialState = ReactToBookStateChangedState.empty
      .setEvents(List(bookHoldCanceled))
      .setBooks(List(bookOnHold))
    expectedState = initialState.clearEvents.setBooks(
      List(AvailableBook.from(bookOnHold, bookHoldCanceled)),
    )
  yield TestCase(initialState, expectedState)

  final private val bookHoldExpired: Gen[TestCase] = for
    bookHoldExpired <- bookHoldExpiredGen
    bookType <- Gen.oneOf(BookType.values.toList)
    holdTill <- Gen.option(instantArbitrary.arbitrary)
    bookOnHold = BookOnHold(
      bookHoldExpired.bookId,
      bookType,
      bookHoldExpired.libraryBranchId,
      bookHoldExpired.patronId,
      holdTill,
    )
    initialState = ReactToBookStateChangedState.empty
      .setEvents(List(bookHoldExpired))
      .setBooks(List(bookOnHold))
    expectedState = initialState.clearEvents.setBooks(
      List(AvailableBook.from(bookOnHold, bookHoldExpired)),
    )
  yield TestCase(initialState, expectedState)

  final private val availableBookIsPlacedOnHold: Gen[TestCase] = for
    bookPlacedOnHold <- bookPlacedOnHoldGen()
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

  final private val bookPlacedOnHoldAlreadyBySamePatron: Gen[TestCase] = for
    bookPlacedOnHold <- bookPlacedOnHoldGen()
    holdTill <- Gen.option(instantArbitrary.arbitrary)
    initialState = ReactToBookStateChangedState.empty
      .setEvents(List(bookPlacedOnHold))
      .setBooks(
        List(
          BookOnHold(
            bookPlacedOnHold.bookId,
            bookPlacedOnHold.bookType,
            bookPlacedOnHold.libraryBranchId,
            bookPlacedOnHold.patronId,
            holdTill,
          ),
        ),
      )
    expectedState = initialState.clearEvents
  yield TestCase(initialState, expectedState)

  final private val bookPlacedOnHoldAlreadyByOtherPatron: Gen[TestCase] = for
    patronId1 :: patronId2 :: Nil <- nDistinct(2, patronIdGen)
    bookPlacedOnHold <- bookPlacedOnHoldGen(patronId1)
    bookOnHold = BookOnHold(
      bookPlacedOnHold.bookId,
      bookPlacedOnHold.bookType,
      bookPlacedOnHold.libraryBranchId,
      patronId2,
      bookPlacedOnHold.holdTill,
    )
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    initialState = ReactToBookStateChangedState.empty
      .setEvents(List(bookPlacedOnHold))
      .setBooks(List(bookOnHold))
      .setInstants(List(when))
      .setUUIDs(List(eventId.value))
    expectedState = initialState.clearEvents.clearInstants.clearUUIDs.setErrors(
      List(BookDuplicateHoldFound.from(eventId, when, bookOnHold, bookPlacedOnHold)),
    )
  yield TestCase(initialState, expectedState)

  final private val bookOnHoldIsReturned: Gen[TestCase] = for
    bookReturned <- bookReturnedGen
    holdTill <- Gen.option(instantArbitrary.arbitrary)
    bookOnHold = BookOnHold(
      bookReturned.bookId,
      bookReturned.bookType,
      bookReturned.libraryBranchId,
      bookReturned.patronId,
      holdTill,
    )
    initialState = ReactToBookStateChangedState.empty
      .setEvents(List(bookReturned))
      .setBooks(List(bookOnHold))
    expectedState = initialState.clearEvents.setBooks(
      List(AvailableBook.from(bookOnHold, bookReturned)),
    )
  yield TestCase(initialState, expectedState)
