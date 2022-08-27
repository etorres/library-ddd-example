package es.eriktorr.library
package lending.integration

import lending.infrastructure.LendingGenerators.availableBookGen
import lending.infrastructure.JdbcAvailableBooks
import shared.infrastructure.{JdbcTestConfig, JdbcTransactorsSuite}

import org.scalacheck.effect.PropF.forAllF

final class JdbcAvailableBooksSuite extends JdbcTransactorsSuite:
  override def jdbcTestConfig: JdbcTestConfig = JdbcTestConfig.AvailableBooksForLending

  test("should create an available book for lending") {
    forAllF(availableBookGen) { availableBook =>
      val availableBooks = JdbcAvailableBooks(transactorFixture())
      for
        _ <- availableBooks.add(availableBook)
        _ <- availableBooks.findBy(availableBook.bookId).assertEquals(Some(availableBook))
      yield ()
    }
  }
