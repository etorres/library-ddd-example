package es.eriktorr.library
package lending.model

import book.model.BookType
import lending.model.Book.AvailableBook
import lending.model.HoldDurationType.HoldDuration
import lending.model.Patron.PatronHoldsAndOverdueCheckouts
import lending.model.PlacingOnHoldPolicy.PlacingOnHoldDecision
import lending.model.PlacingOnHoldPolicy.PlacingOnHoldDecisionType.{Allowance, Rejection}

import java.time.Instant
import scala.concurrent.duration.Deadline

trait PlacingOnHoldPolicy:
  def canHold(
      availableBook: AvailableBook,
      patronHoldsAndOverdueCheckouts: PatronHoldsAndOverdueCheckouts,
      holdDuration: HoldDuration,
  ): PlacingOnHoldDecision

object PlacingOnHoldPolicy:
  enum PlacingOnHoldDecisionType[+A]:
    case Allowance extends PlacingOnHoldDecisionType[Nothing]
    case Rejection(reason: A) extends PlacingOnHoldDecisionType[A]

  val maximumOverdueCheckouts: Int = 2

  val maximumHolds: Int = 5

  type PlacingOnHoldDecision = PlacingOnHoldDecisionType[String | Nothing]

  val onlyResearcherPatronsCanHoldRestrictedBooksPolicy: PlacingOnHoldPolicy =
    (
        availableBook: AvailableBook,
        patronHoldsAndOverdueCheckouts: PatronHoldsAndOverdueCheckouts,
        _: HoldDuration,
    ) =>
      if availableBook.bookType == BookType.Restricted && patronHoldsAndOverdueCheckouts.patronType == PatronType.Regular
      then Rejection("Regular patrons cannot hold restricted books")
      else Allowance

  val onlyResearcherPatronsCanPlaceOpenEndedHolds: PlacingOnHoldPolicy =
    (
        _: AvailableBook,
        patronHoldsAndOverdueCheckouts: PatronHoldsAndOverdueCheckouts,
        holdDuration: HoldDuration,
    ) =>
      if patronHoldsAndOverdueCheckouts.patronType == PatronType.Regular && holdDuration == HoldDurationType.OpenEnded
      then Rejection("Regular patron cannot place open ended holds")
      else Allowance

  val overdueCheckoutsRejectionPolicy: PlacingOnHoldPolicy =
    (
        availableBook: AvailableBook,
        patronHoldsAndOverdueCheckouts: PatronHoldsAndOverdueCheckouts,
        _: HoldDuration,
    ) =>
      if patronHoldsAndOverdueCheckouts.overdueCheckoutsAt(
          availableBook.libraryBranchId,
        ) >= maximumOverdueCheckouts
      then Rejection("Cannot place on hold when there are overdue checkouts")
      else Allowance

  val regularPatronMaximumNumberOfHoldsPolicy: PlacingOnHoldPolicy = (
      _: AvailableBook,
      patronHoldsAndOverdueCheckouts: PatronHoldsAndOverdueCheckouts,
      _: HoldDuration,
  ) =>
    if patronHoldsAndOverdueCheckouts.patronType == PatronType.Regular && patronHoldsAndOverdueCheckouts.numberOfHolds >= maximumHolds
    then Rejection("Patron cannot hold more books")
    else Allowance
