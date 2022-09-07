package es.eriktorr.library
package lending.infrastructure

import lending.infrastructure.InMemoryPlacingOnHoldPolicies.PlacingOnHoldPoliciesState
import lending.model.PlacingOnHoldPolicy.{
  onlyResearcherPatronsCanHoldRestrictedBooksPolicy,
  onlyResearcherPatronsCanPlaceOpenEndedHolds,
  overdueCheckoutsRejectionPolicy,
  regularPatronMaximumNumberOfHoldsPolicy,
}
import lending.model.{PlacingOnHoldPolicies, PlacingOnHoldPolicy}

import cats.effect.{IO, Ref}

final class InMemoryPlacingOnHoldPolicies private (
    placingOnHoldPoliciesStateRef: Ref[IO, PlacingOnHoldPoliciesState],
) extends PlacingOnHoldPolicies:
  override def allCurrentPolicies: IO[List[PlacingOnHoldPolicy]] =
    placingOnHoldPoliciesStateRef.get.map(_.policies)

object InMemoryPlacingOnHoldPolicies:
  final private case class PlacingOnHoldPoliciesState(policies: List[PlacingOnHoldPolicy])

  def impl: IO[InMemoryPlacingOnHoldPolicies] =
    Ref
      .of[IO, PlacingOnHoldPoliciesState](
        PlacingOnHoldPoliciesState(
          List(
            onlyResearcherPatronsCanHoldRestrictedBooksPolicy,
            onlyResearcherPatronsCanPlaceOpenEndedHolds,
            overdueCheckoutsRejectionPolicy,
            regularPatronMaximumNumberOfHoldsPolicy,
          ),
        ),
      )
      .map(InMemoryPlacingOnHoldPolicies.apply(_))
