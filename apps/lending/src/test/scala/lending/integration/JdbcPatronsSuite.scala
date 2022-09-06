package es.eriktorr.library
package lending.integration

import book.infrastructure.BookInstanceGenerators.bookIdGen
import lending.infrastructure.LendingGenerators.{
  bookGen,
  patronGen,
  patronHoldGen,
  patronIdGen,
  patronOverdueCheckoutGen,
}
import lending.infrastructure.{JdbcBooks, JdbcPatrons, JdbcPatronsWriter}
import lending.integration.JdbcPatronsSuite.{testCaseGen, TestCase}
import lending.model.Patron.{Hold, OverdueCheckout, PatronHoldsAndOverdueCheckouts}
import lending.model.{Book, Patron, PatronId}
import shared.infrastructure.CollectionGenerators.nDistinct
import shared.infrastructure.{JdbcTestConfig, JdbcTransactorsSuite}

import cats.syntax.all.*
import org.scalacheck.Gen
import org.scalacheck.cats.implicits.*
import org.scalacheck.effect.PropF.forAllF

final class JdbcPatronsSuite extends JdbcTransactorsSuite:
  override def jdbcTestConfig: JdbcTestConfig = JdbcTestConfig.LendingPatrons

  override def scalaCheckInitialSeed = "1HCIp1UcyrY_oJFzsG7uMITTJ2NRqLheO24dKy9cvLF=" // TODO

  test("should find holds and overdue checkouts by patron Id") {
    forAllF(testCaseGen) {
      case TestCase(
            patronId,
            patronHoldsAndOverdueCheckouts,
            allBooks,
            allPatronHoldsAndOverdueCheckouts,
          ) =>
        val transactor = transactorFixture()
        val (books, patrons, patronsWriter) =
          (JdbcBooks(transactor), JdbcPatrons(transactor), JdbcPatronsWriter(transactor))
        for
          _ <- allBooks.traverse(books.save)
          _ <- allPatronHoldsAndOverdueCheckouts.traverse { item =>
            for
              _ <- patronsWriter.save(item.patron)
              _ <- item.holds.traverse(patronsWriter.save(item.patronId, _))
              _ <- item.overdueCheckouts.traverse(patronsWriter.save(item.patronId, _))
            yield ()
          }
          _ <- patrons
            .findBy(patronId)
            .map { result =>
              println(s"\n\n >> ACTUAL: $result\n")
              result
            }
            .assertEquals(patronHoldsAndOverdueCheckouts)
        yield ()
    }
  }

object JdbcPatronsSuite:
  final private case class TestCase(
      patronId: PatronId,
      patronHoldsAndOverdueCheckouts: Option[PatronHoldsAndOverdueCheckouts],
      allBooks: List[Book],
      allPatronHoldsAndOverdueCheckouts: List[PatronHoldsAndOverdueCheckouts],
  )

  private[this] def holdsAndOverdueCheckoutsFrom(patron: Patron, books: List[Book]) = for
    holdsAndOverdueCheckouts <- books.traverse[Gen, Hold | OverdueCheckout](book =>
      Gen.frequency[Hold | OverdueCheckout](
        1 -> patronHoldGen(book.bookId),
        1 -> patronOverdueCheckoutGen(book.bookId),
      ),
    )
    holds = holdsAndOverdueCheckouts.collect { case value: Hold => value }
    overdueCheckouts = holdsAndOverdueCheckouts.collect { case value: OverdueCheckout => value }
  yield PatronHoldsAndOverdueCheckouts(patron, holds, overdueCheckouts)

  private val testCaseGen: Gen[TestCase] = for
    selectedPatronId :: otherPatronId :: Nil <- nDistinct(2, patronIdGen)
    (selectedPatron, otherPatron) <- (patronGen(selectedPatronId), patronGen(otherPatronId)).tupled
    bookIds <- nDistinct(12, bookIdGen)
    books <- bookIds.traverse(bookId => bookGen(bookId))
    (selectedBooks, otherBooks) = books.splitAt(4)
    (selectedHoldsAndOverdueCheckouts, otherHoldsAndOverdueCheckouts) <- (
      holdsAndOverdueCheckoutsFrom(selectedPatron, selectedBooks),
      holdsAndOverdueCheckoutsFrom(otherPatron, otherBooks),
    ).tupled
  yield TestCase(
    selectedPatronId,
    Some(selectedHoldsAndOverdueCheckouts),
    selectedBooks ++ otherBooks,
    List(selectedHoldsAndOverdueCheckouts, otherHoldsAndOverdueCheckouts),
  )
