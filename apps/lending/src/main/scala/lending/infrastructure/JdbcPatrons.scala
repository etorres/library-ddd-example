package es.eriktorr.library
package lending.infrastructure

import book.infrastructure.BookIdJdbcMapping
import book.model.BookId
import lending.infrastructure.JdbcPatrons.{
  PatronHoldsAndOverdueCheckoutsJdbcMapping,
  RawPatronHoldsAndOverdueCheckouts,
}
import lending.infrastructure.PatronTypeJdbcMapping
import lending.model.*
import lending.model.Patron.{Hold, OverdueCheckout, PatronHoldsAndOverdueCheckouts}
import shared.infrastructure.InvalidDatabaseEntity
import shared.validated.AllErrorsOr
import shared.validated.ValidatedIO.validatedNecIO

import cats.data.{NonEmptyList, ValidatedNec}
import cats.effect.IO
import cats.syntax.apply.*
import cats.syntax.validated.*
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.time.Instant
import scala.annotation.tailrec

final class JdbcPatrons(transactor: Transactor[IO])
    extends Patrons
    with PatronHoldsAndOverdueCheckoutsJdbcMapping:
  override def findBy(patronId: PatronId): IO[Option[PatronHoldsAndOverdueCheckouts]] = sql"""
        SELECT 
          patrons.patron_id AS patron_id,
          patrons.patron_type AS patron_type, 
          NULL::UUID AS holds_book_id,
          NULL::UUID AS holds_library_branch_id,
          NULL::TIMESTAMPTZ AS holds_till,
          NULL::UUID AS overdue_checkouts_book_id,
          NULL::UUID AS overdue_checkouts_library_branch_id
        FROM patrons
        WHERE patrons.patron_id = $patronId

        UNION
                                                                                
        SELECT 
          NULL::UUID AS patron_id,
          NULL::VARCHAR AS patron_type, 
          book_id AS holds_book_id, 
          library_branch_id AS holds_library_branch_id, 
          till AS holds_till,
          NULL::UUID AS overdue_checkouts_book_id,
          NULL::UUID AS overdue_checkouts_library_branch_id
        FROM patrons
        JOIN holds ON holds.patron_id = patrons.patron_id
        WHERE patrons.patron_id = $patronId
  
        UNION
  
        SELECT 
          NULL::UUID AS patron_id,
          NULL::VARCHAR AS patron_type, 
          NULL::UUID AS holds_book_id,
          NULL::UUID AS holds_library_branch_id,
          NULL::TIMESTAMPTZ AS holds_till,
          book_id AS overdue_checkouts_book_id, 
          library_branch_id AS overdue_checkouts_library_branch_id 
        FROM patrons
        JOIN overdue_checkouts ON overdue_checkouts.patron_id = patrons.patron_id
        WHERE patrons.patron_id = $patronId"""
    .query[RawPatronHoldsAndOverdueCheckouts]
    .to[List]
    .transact(transactor)
    .flatMap { items =>
      items match
        case ::(head, next) =>
          RawPatronHoldsAndOverdueCheckouts
            .unwrap(NonEmptyList.of(head, next*))
            .validated
            .map(Some.apply)
        case Nil => IO.pure(None)
    }

object JdbcPatrons:
  trait PatronHoldsAndOverdueCheckoutsJdbcMapping
      extends BookIdJdbcMapping
      with LibraryBranchIdJdbcMapping
      with PatronIdJdbcMapping
      with PatronTypeJdbcMapping

  final case class RawPatronHoldsAndOverdueCheckouts(
      patronId: Option[PatronId],
      patronType: Option[PatronType],
      holdBookId: Option[BookId],
      holdLibraryBranchId: Option[LibraryBranchId],
      holdTill: Option[Instant],
      overdueCheckoutBookId: Option[BookId],
      overdueCheckoutLibraryBranchId: Option[LibraryBranchId],
  )

  object RawPatronHoldsAndOverdueCheckouts:
    private[this] def parse(
        item: RawPatronHoldsAndOverdueCheckouts,
    ): AllErrorsOr[Hold | OverdueCheckout | Patron] =
      val hold = for
        bookId <- item.holdBookId
        libraryBranchId <- item.holdLibraryBranchId
        till <- item.holdTill
      yield Hold(bookId, libraryBranchId, till)

      val overdueCheckout = for
        bookId <- item.overdueCheckoutBookId
        libraryBranchId <- item.overdueCheckoutLibraryBranchId
      yield OverdueCheckout(bookId, libraryBranchId)

      val patron = for
        patronId <- item.patronId
        patronType <- item.patronType
      yield Patron(patronId, patronType)

      List[Option[Hold | OverdueCheckout | Patron]](hold, overdueCheckout, patron).collect {
        case Some(value) =>
          value
      } match
        case Nil =>
          InvalidDatabaseEntity(s"One or more required field(s) are missing: $item").invalidNec
        case head :: Nil => head.validNec
        case ::(_, _) => InvalidDatabaseEntity(s"Multiple criteria are met: $item").invalidNec

    def unwrap(
        items: NonEmptyList[RawPatronHoldsAndOverdueCheckouts],
    ): AllErrorsOr[PatronHoldsAndOverdueCheckouts] =
      @tailrec
      def combineAll(
          validateItems: List[AllErrorsOr[Hold | OverdueCheckout | Patron]],
          accumulated: AllErrorsOr[List[Hold | OverdueCheckout | Patron]],
      ): AllErrorsOr[List[Hold | OverdueCheckout | Patron]] = validateItems match
        case Nil => accumulated
        case ::(head, next) => combineAll(next, accumulated.combine(head.map(List(_))))

      val validatedItems = combineAll(items.toList.map(parse), List.empty.validNec)

      val patron: AllErrorsOr[Patron] = validatedItems
        .map(_.collectFirst { case value: Patron =>
          value
        })
        .andThen(_.fold(InvalidDatabaseEntity("No patron found").invalidNec)(identity(_).validNec))
      val holds: AllErrorsOr[List[Hold]] = validatedItems.map(_.collect { case hold: Hold =>
        hold
      })
      val overdueCheckouts: AllErrorsOr[List[OverdueCheckout]] = validatedItems.map(_.collect {
        case overdueCheckout: OverdueCheckout => overdueCheckout
      })
      (patron, holds, overdueCheckouts).mapN(PatronHoldsAndOverdueCheckouts.apply)
