package es.eriktorr.library
package catalogue.integration

import book.infrastructure.BookGenerators.{bookGen, bookInstanceGen}
import book.model.{Book, BookInstance}
import catalogue.infrastructure.JdbcCatalogue
import catalogue.integration.JdbcCatalogueSuite.{testCaseGen, TestCase}
import shared.infrastructure.{JdbcTestConfig, JdbcTransactorsSuite}

import cats.effect.IO
import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class JdbcCatalogueSuite extends JdbcTransactorsSuite:
  override def jdbcTestConfig: JdbcTestConfig = JdbcTestConfig.Catalogue

  test("should add a new book instance to the catalogue") {
    forAllF(testCaseGen) { case TestCase(book, bookInstance) =>
      val catalogue = JdbcCatalogue(transactorFixture())
      for
        _ <- catalogue.add(book).assertEquals((), "add new book")
        _ <- catalogue.findBy(book.isbn).assertEquals(Some(book), "find a book by its ISBN")
        _ <- catalogue.add(bookInstance).assertEquals((), "add new book instance")
      yield ()
    }
  }

  test("should reject adding a new book instance to the catalogue when book isbn does not exist") {
    forAllF(bookInstanceGen()) { bookInstance =>
      val catalogue = JdbcCatalogue(transactorFixture())
      val expectedErrorMessage =
        s"""ERROR: insert or update on table "book_instance_catalogue" violates foreign key constraint "book_instance_catalogue_isbn_fkey"
           |  Detail: Key (isbn)=(${bookInstance.isbn}) is not present in table "book_catalogue".""".stripMargin
      catalogue
        .add(bookInstance)
        .interceptMessage[org.postgresql.util.PSQLException](expectedErrorMessage)
        .map(_ => ())
    }
  }

object JdbcCatalogueSuite:
  final private case class TestCase(book: Book, bookInstance: BookInstance)

  private val testCaseGen: Gen[TestCase] = for
    book <- bookGen
    bookInstance <- bookInstanceGen(book.isbn)
  yield TestCase(book, bookInstance)
