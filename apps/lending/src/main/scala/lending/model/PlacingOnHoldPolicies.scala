package es.eriktorr.library
package lending.model

import cats.effect.IO

trait PlacingOnHoldPolicies:
  def allCurrentPolicies: IO[List[PlacingOnHoldPolicy]]
