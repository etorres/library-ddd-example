package es.eriktorr.library
package catalogue.integration

import book.model.Book
import catalogue.infrastructure.JdbcCatalogue
import infrastructure.jdbc.JdbcTransactorsSuite
import validated.ValidatedIO.validatedNecIO

final class JdbcCatalogueSuite extends JdbcTransactorsSuite:
  override def currentSchema: String = "test_catalogue_book"

  test("should add a new book to the catalogue") {
    for
      book <- Book
        .from(
          isbn = "0321125215",
          title = "Domain-Driven Design: Tackling Complexity in the Heart of Software",
          author = "Eric Evans",
        )
        .validated
      catalogue = JdbcCatalogue(transactorFixture())
      _ <- catalogue.add(book).assertEquals(())
    yield ()
  }
