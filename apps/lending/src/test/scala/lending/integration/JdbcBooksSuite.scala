package es.eriktorr.library
package lending.integration

import lending.infrastructure.LendingGenerators.availableBookGen
import lending.infrastructure.JdbcBooks
import shared.infrastructure.{JdbcTestConfig, JdbcTransactorsSuite}

import org.scalacheck.effect.PropF.forAllF

final class JdbcBooksSuite extends JdbcTransactorsSuite:
  override def jdbcTestConfig: JdbcTestConfig = JdbcTestConfig.Lending

  test("should create an available book for lending") {
    forAllF(availableBookGen) { availableBook =>
      val availableBooks = JdbcBooks(transactorFixture())
      for
        _ <- availableBooks.save(availableBook)
        _ <- availableBooks.findBy(availableBook.bookId).assertEquals(Some(availableBook))
      yield ()
    }
  }
