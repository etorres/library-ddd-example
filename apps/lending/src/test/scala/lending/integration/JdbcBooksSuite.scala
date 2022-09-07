package es.eriktorr.library
package lending.integration

import lending.infrastructure.JdbcBooks
import lending.infrastructure.LendingGenerators.{bookGen, patronIdGen}
import lending.integration.JdbcBooksSuite.{testCaseGen, TestCase}
import lending.model.Book
import lending.model.Book.{AvailableBook, BookOnHold, CheckedOutBook}
import shared.infrastructure.TimeGenerators.instantArbitrary
import shared.infrastructure.{JdbcTestConfig, JdbcTransactorsSuite}

import org.scalacheck.Gen
import org.scalacheck.effect.PropF.forAllF

final class JdbcBooksSuite extends JdbcTransactorsSuite:
  override def jdbcTestConfig: JdbcTestConfig = JdbcTestConfig.LendingBooks

  test("should create and update a book in the lending database") {
    forAllF(testCaseGen) { case TestCase(book, updatedBook) =>
      val books = JdbcBooks(transactorFixture())
      for
        _ <- books.save(book)
        _ <- books.save(updatedBook)
        _ <- books.findBy(book.bookId).assertEquals(Some(updatedBook))
      yield ()
    }
  }

object JdbcBooksSuite:
  final private case class TestCase(book: Book, updatedBook: Book)

  private val testCaseGen: Gen[TestCase] = for
    book <- bookGen()
    patronId <- patronIdGen
    holdTill <- Gen.option(instantArbitrary.arbitrary)
    updatedBook <- bookGen().map { otherBook =>
      otherBook match
        case AvailableBook(_, bookType, libraryBranchId) =>
          BookOnHold(book.bookId, bookType, libraryBranchId, patronId, holdTill)
        case BookOnHold(_, bookType, holdPlacedAt, byPatron, _) =>
          CheckedOutBook(book.bookId, bookType, holdPlacedAt, byPatron)
        case CheckedOutBook(_, bookType, checkedOutAt, _) =>
          AvailableBook(book.bookId, bookType, checkedOutAt)
    }
  yield TestCase(book, updatedBook)
