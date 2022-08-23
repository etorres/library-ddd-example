package es.eriktorr.library
package catalogue.integration

import book.model.infrastructure.BookGenerators.bookGen
import catalogue.infrastructure.JdbcCatalogue
import infrastructure.jdbc.JdbcTransactorsSuite

import org.scalacheck.effect.PropF.forAllF

final class JdbcCatalogueSuite extends JdbcTransactorsSuite:
  override def currentSchema: String = "test_book_catalogue"

  test("should add a new book to the catalogue") {
    forAllF(bookGen) { book =>
      val catalogue = JdbcCatalogue(transactorFixture())
      catalogue.add(book).assertEquals(())
    }
  }

  test("should add a new book instance to catalogue") {
    ???
  }
