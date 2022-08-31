package es.eriktorr.library
package catalogue.infrastructure

import book.infrastructure.BookInstanceGenerators.isbnGen
import catalogue.model.{Author, Book, Title}

import org.scalacheck.Gen

object CatalogueGenerators:
  def textGen(minLength: Int = 3, maxLength: Int = 10): Gen[String] = for
    length <- Gen.choose(minLength, maxLength)
    text <- Gen.listOfN[Char](length, Gen.asciiPrintableChar).map(_.mkString)
  yield text

  val bookGen: Gen[Book] = for
    isbn <- isbnGen
    title <- textGen().map(Title.unsafeFrom)
    author <- textGen().map(Author.unsafeFrom)
  yield Book(isbn, title, author)
