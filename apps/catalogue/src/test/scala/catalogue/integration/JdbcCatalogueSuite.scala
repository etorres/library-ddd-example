package es.eriktorr.library
package catalogue.integration

import book.model.{Author, Book, ISBN, Title}
import catalogue.infrastructure.JdbcCatalogue
import infrastructure.jdbc.JdbcTransactorsSuite
import validated.*

import cats.effect.IO

final class JdbcCatalogueSuite extends JdbcTransactorsSuite:
  override def currentSchema: String = "test_catalogue_book"

  test("should add a new book to the catalogue") {
    val catalogue = JdbcCatalogue(transactorFixture())
    Book
      .from(
        isbn = "0321125215",
        title = "Domain-Driven Design: Tackling Complexity in the Heart of Software",
        author = "Eric Evans",
      )
      .validate
    catalogue
      .add(
        Book(
          ISBN.unsafeFrom("0321125215"),
          Title.unsafeFrom("Domain-Driven Design: Tackling Complexity in the Heart of Software"),
          Author.unsafeFrom("Eric Evans"),
        ),
      )
      .assertEquals(())
  }
