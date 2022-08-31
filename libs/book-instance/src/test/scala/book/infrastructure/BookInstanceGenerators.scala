package es.eriktorr.library
package book.infrastructure

import book.model.*
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.refined.types.infrastructure.RefinedTypesGenerators.eventIdGen

import org.scalacheck.Gen

object BookInstanceGenerators:
  val bookIdGen: Gen[BookId] = Gen.uuid.map(BookId.from)

  val isbnGen: Gen[ISBN] = for
    firstNineDigits <- Gen.containerOfN[List, Char](9, Gen.numChar)
    checkDigit <- Gen.frequency(1 -> Gen.const('X'), 1 -> Gen.numChar)
  yield ISBN.unsafeFrom((firstNineDigits :+ checkDigit).mkString(""))

  def bookInstanceGen(isbnGen: Gen[ISBN] = isbnGen): Gen[BookInstance] = for
    bookId <- bookIdGen
    isbn <- isbnGen
    bookType <- Gen.oneOf(BookType.values.toList)
  yield BookInstance(bookId, isbn, bookType)

  val bookInstanceAddedToCatalogueGen: Gen[BookInstanceAddedToCatalogue] = for
    eventId <- eventIdGen
    when <- instantArbitrary.arbitrary
    bookInstance <- bookInstanceGen()
  yield BookInstanceAddedToCatalogue(eventId, when, bookInstance)
