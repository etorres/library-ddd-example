package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookInstanceGenerators.bookIdGen
import book.model.{BookId, BookType}
import lending.model.*
import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import lending.model.BookStateChanged.*
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.eventIdGen

import org.scalacheck.Gen

object LendingGenerators:
  val libraryBranchIdGen: Gen[LibraryBranchId] = Gen.uuid.map(LibraryBranchId.from)

  val patronIdGen: Gen[PatronId] = Gen.uuid.map(PatronId.from)

  private[this] def availableBookGen(bookIdGen: Gen[BookId]): Gen[AvailableBook] = for
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- libraryBranchIdGen
  yield AvailableBook(bookId, bookType, libraryBranchId)

  private[this] def bookOnHoldGen(bookIdGen: Gen[BookId]): Gen[BookOnHold] = for
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    holdPlacedAt <- libraryBranchIdGen
    byPatron <- patronIdGen
    holdTill <- Gen.option(instantArbitrary.arbitrary)
  yield BookOnHold(bookId, bookType, holdPlacedAt, byPatron, holdTill)

  private[this] def checkedOutBookGen(bookIdGen: Gen[BookId]): Gen[CheckedOutBook] = for
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    checkedOutAt <- libraryBranchIdGen
    byPatron <- patronIdGen
  yield CheckedOutBook(bookId, bookType, checkedOutAt, byPatron)

  def bookGen(bookIdGen: Gen[BookId] = bookIdGen): Gen[Book] =
    Gen.frequency(
      1 -> availableBookGen(bookIdGen),
      1 -> bookOnHoldGen(bookIdGen),
      1 -> checkedOutBookGen(bookIdGen),
    )

  val bookCheckedOutGen: Gen[BookCheckedOut] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- libraryBranchIdGen
    till <- instantArbitrary.arbitrary
  yield BookCheckedOut(eventId, when, patronId, bookId, bookType, libraryBranchId, till)

  val bookHoldCanceledGen: Gen[BookHoldCanceled] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    libraryBranchId <- libraryBranchIdGen
  yield BookHoldCanceled(eventId, when, patronId, bookId, libraryBranchId)

  val bookHoldExpiredGen: Gen[BookHoldExpired] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    libraryBranchId <- libraryBranchIdGen
  yield BookHoldExpired(eventId, when, patronId, bookId, libraryBranchId)

  def bookPlacedOnHoldGen(patronIdGen: Gen[PatronId] = patronIdGen): Gen[BookPlacedOnHold] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- libraryBranchIdGen
    holdFrom <- instantArbitrary.arbitrary
    holdTill <- Gen.option(instantArbitrary.arbitrary)
  yield BookPlacedOnHold(
    eventId,
    when,
    patronId,
    bookId,
    bookType,
    libraryBranchId,
    holdFrom,
    holdTill,
  )

  val bookReturnedGen: Gen[BookReturned] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- libraryBranchIdGen
  yield BookReturned(eventId, when, patronId, bookId, bookType, libraryBranchId)

  val bookStateChangedGen: Gen[BookStateChanged] =
    Gen.frequency(
      1 -> bookCheckedOutGen,
      1 -> bookHoldCanceledGen,
      1 -> bookHoldExpiredGen,
      1 -> bookPlacedOnHoldGen(),
      1 -> bookReturnedGen,
    )

  val bookDuplicateHoldFoundGen: Gen[BookDuplicateHoldFound] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    firstPatronId <- patronIdGen
    secondPatronId <- patronIdGen
    libraryBranchId <- libraryBranchIdGen
    bookId <- bookIdGen
  yield BookDuplicateHoldFound(
    eventId,
    when,
    firstPatronId,
    secondPatronId,
    libraryBranchId,
    bookId,
  )

  def patronGen(patronIdGen: Gen[PatronId] = patronIdGen): Gen[Patron] = for
    patronId <- patronIdGen
    patronType <- Gen.oneOf(PatronType.values.toList)
  yield Patron(patronId, patronType)

  def patronHoldGen(bookIdGen: Gen[BookId]): Gen[Patron.Hold] = for
    bookId <- bookIdGen
    libraryBranchId <- libraryBranchIdGen
    till <- instantArbitrary.arbitrary
  yield Patron.Hold(bookId, libraryBranchId, till)

  def patronOverdueCheckoutGen(bookIdGen: Gen[BookId]): Gen[Patron.OverdueCheckout] = for
    bookId <- bookIdGen
    libraryBranchId <- libraryBranchIdGen
  yield Patron.OverdueCheckout(bookId, libraryBranchId)
