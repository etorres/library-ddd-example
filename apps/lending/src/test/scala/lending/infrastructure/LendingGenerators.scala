package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookInstanceGenerators.bookIdGen
import book.model.BookType
import lending.model.Book.AvailableBook
import lending.model.BookStateChanged.*
import lending.model.{BookStateChanged, LibraryBranchId, PatronId}
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.eventIdGen

import org.scalacheck.Gen

object LendingGenerators:
  val libraryBranchIdGen: Gen[LibraryBranchId] = Gen.uuid.map(LibraryBranchId.from)

  val patronIdGen: Gen[PatronId] = Gen.uuid.map(PatronId.from)

  val availableBookGen: Gen[AvailableBook] = for
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- libraryBranchIdGen
  yield AvailableBook(bookId, bookType, libraryBranchId)

  private[this] val bookCheckedOutGen: Gen[BookCheckedOut] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- libraryBranchIdGen
    till <- instantArbitrary.arbitrary
  yield BookCheckedOut(eventId, when, patronId, bookId, bookType, libraryBranchId, till)

  private[this] val bookHoldCanceledGen: Gen[BookHoldCanceled] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    libraryBranchId <- libraryBranchIdGen
  yield BookHoldCanceled(eventId, when, patronId, bookId, libraryBranchId)

  private[this] val bookHoldExpiredGen: Gen[BookHoldExpired] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    patronId <- patronIdGen
    bookId <- bookIdGen
    libraryBranchId <- libraryBranchIdGen
  yield BookHoldExpired(eventId, when, patronId, bookId, libraryBranchId)

  private[this] val bookPlacedOnHoldGen: Gen[BookPlacedOnHold] = for
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

  private[this] val bookReturnedGen: Gen[BookReturned] = for
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
      1 -> bookPlacedOnHoldGen,
      1 -> bookReturnedGen,
    )
