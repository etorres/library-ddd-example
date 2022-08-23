package es.eriktorr.library
package book.model

import org.scalacheck.Gen

object BookGenerators:
  val isbnGen: Gen[ISBN] = for
    firstNineDigits <- Gen.containerOfN[List, Char](9, Gen.numChar)
    checkDigit <- Gen.frequency(1 -> Gen.const('X'), 1 -> Gen.numChar)
  yield ISBN.unsafeFrom((firstNineDigits :+ checkDigit).mkString(""))

  def textGen(minLength: Int = 3, maxLength: Int = 10): Gen[String] = for
    length <- Gen.choose(minLength, maxLength)
    text <- Gen.listOfN[Char](length, Gen.asciiPrintableChar).map(_.mkString)
  yield text

  val bookGen: Gen[Book] = for
    isbn <- isbnGen
    title <- textGen().map(Title.unsafeFrom)
    author <- textGen().map(Author.unsafeFrom)
  yield Book(isbn, title, author)
