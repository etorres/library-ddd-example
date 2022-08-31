package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookInstanceGenerators.bookIdGen
import book.model.BookType
import lending.model.Book.AvailableBook
import lending.model.LibraryBranchId

import org.scalacheck.Gen

object LendingGenerators:
  val libraryBranchIdGen: Gen[LibraryBranchId] = Gen.uuid.map(LibraryBranchId.from)

  val availableBookGen: Gen[AvailableBook] = for
    bookId <- bookIdGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- libraryBranchIdGen
  yield AvailableBook(bookId, bookType, libraryBranchId)
