package es.eriktorr.library
package lending.infrastructure

import book.model.BookType
import lending.model.AvailableBook
import lending.model.AvailableBook.Book
import shared.refined.types.infrastructure.RefinedTypesGenerators.uuidGen

import org.scalacheck.Gen

object LendingGenerators:
  val availableBookGen: Gen[AvailableBook] = for
    bookId <- uuidGen
    bookType <- Gen.oneOf(BookType.values.toList)
    libraryBranchId <- uuidGen
  yield AvailableBook(Book(bookId, bookType), libraryBranchId)
