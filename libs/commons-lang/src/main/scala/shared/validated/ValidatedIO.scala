package es.eriktorr.library
package shared.validated

import shared.ValidationErrors

import cats.data.Validated
import cats.effect.IO

trait ValidatedIO[A]:
  extension (maybeA: AllErrorsOr[A]) def validated: IO[A]

object ValidatedIO:
  given validatedNecIO[A]: ValidatedIO[A] with
    extension (maybeA: AllErrorsOr[A])
      def validated: IO[A] = maybeA match
        case Validated.Valid(value) => IO.pure(value)
        case Validated.Invalid(errors) => IO.raiseError(ValidationErrors(errors))
