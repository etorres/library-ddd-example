package es.eriktorr.library
package lending.acceptance

import book.model.BookType
import lending.acceptance.ReactToBookStateChangedSuite.*
import lending.infrastructure.LendingGenerators.*
import lending.infrastructure.ReactToBookStateChangedSuiteRunner
import lending.infrastructure.ReactToBookStateChangedSuiteRunner.ReactToBookStateChangedState
import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import lending.model.{Book, BookDuplicateHoldFound, BookStateChanged}
import shared.infrastructure.CollectionGenerators.nDistinct
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.eventIdGen

import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class ReactToBookStateChangedSuite extends CatsEffectSuite with ScalaCheckEffectSuite:

  test("should check out book which is marked as placed on hold in the system") {
    checkUsing(bookOnHoldIsCheckedOut)
  }

  test("should make book available when hold canceled") {
    checkUsing(bookHoldIsCanceled)
  }

  test("should make book available when hold expired") {
    checkUsing(bookHoldExpired)
  }

  test("should place on hold book which is marked as available in the system") {
    checkUsing(availableBookIsPlacedOnHold)
  }

  test(
    "should not raise anything if book is on hold by the same patron",
  ) {
    checkUsing(bookPlacedOnHoldAlreadyBySamePatron)
  }

  test(
    "should raise duplicate hold found event when someone placed on hold book already on hold",
  ) {
    checkUsing(bookPlacedOnHoldAlreadyByOtherPatron)
  }

  test("should return book which is marked as placed on hold in the system") {
    checkUsing(bookOnHoldIsReturned)
  }

  test("should do nothing when the state change is illegal") {
    checkUsing(illegalBookStateChange)
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

  final private[this] val illegalBookCheckedOut: Gen[(BookStateChanged, Book)] = for
    bookCheckedOut <- bookCheckedOutGen
    book <- Gen.frequency(
      1 -> AvailableBook(
        bookCheckedOut.bookId,
        bookCheckedOut.bookType,
        bookCheckedOut.libraryBranchId,
      ),
      1 -> CheckedOutBook(
        bookCheckedOut.bookId,
        bookCheckedOut.bookType,
        bookCheckedOut.libraryBranchId,
        bookCheckedOut.patronId,
      ),
    )
  yield (bookCheckedOut, book)

  final private[this] val illegalBookHoldCanceled: Gen[(BookStateChanged, Book)] = for
    bookHoldCanceled <- bookHoldCanceledGen
    bookType <- Gen.oneOf(BookType.values.toList)
    book <- Gen.frequency(
      1 -> AvailableBook(
        bookHoldCanceled.bookId,
        bookType,
        bookHoldCanceled.libraryBranchId,
      ),
      1 -> CheckedOutBook(
        bookHoldCanceled.bookId,
        bookType,
        bookHoldCanceled.libraryBranchId,
        bookHoldCanceled.patronId,
      ),
    )
  yield (bookHoldCanceled, book)

  final private[this] val illegalBookHoldExpired: Gen[(BookStateChanged, Book)] = for
    bookHoldExpired <- bookHoldExpiredGen
    bookType <- Gen.oneOf(BookType.values.toList)
    book <- Gen.frequency(
      1 -> AvailableBook(
        bookHoldExpired.bookId,
        bookType,
        bookHoldExpired.libraryBranchId,
      ),
      1 -> CheckedOutBook(
        bookHoldExpired.bookId,
        bookType,
        bookHoldExpired.libraryBranchId,
        bookHoldExpired.patronId,
      ),
    )
  yield (bookHoldExpired, book)

  final private[this] val illegalBookPlacedOnHold: Gen[(BookStateChanged, Book)] = for
    bookPlacedOnHold <- bookPlacedOnHoldGen()
    book = CheckedOutBook(
      bookPlacedOnHold.bookId,
      bookPlacedOnHold.bookType,
      bookPlacedOnHold.libraryBranchId,
      bookPlacedOnHold.patronId,
    )
  yield (bookPlacedOnHold, book)

  final private[this] val illegalBookReturned: Gen[(BookStateChanged, Book)] = for
    bookReturned <- bookReturnedGen
    book <- Gen.frequency(
      1 -> AvailableBook(
        bookReturned.bookId,
        bookReturned.bookType,
        bookReturned.libraryBranchId,
      ),
      1 -> CheckedOutBook(
        bookReturned.bookId,
        bookReturned.bookType,
        bookReturned.libraryBranchId,
        bookReturned.patronId,
      ),
    )
  yield (bookReturned, book)

  final private val illegalBookStateChange: Gen[TestCase] = for
    (event, book) <- Gen.frequency(
      1 -> illegalBookCheckedOut,
      1 -> illegalBookHoldCanceled,
      1 -> illegalBookHoldExpired,
      1 -> illegalBookPlacedOnHold,
      1 -> illegalBookReturned,
    )
    initialState = ReactToBookStateChangedState.empty.setBooks(List(book)).setEvents(List(event))
    expectedState = initialState.clearEvents
  yield TestCase(initialState, expectedState)
