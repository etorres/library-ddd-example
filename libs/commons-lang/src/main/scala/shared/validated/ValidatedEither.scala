package es.eriktorr.library
package shared.validated

import shared.ValidationErrors

import cats.data.Validated

trait ValidatedEither[A]:
  extension (maybeA: AllErrorsOr[A]) def either: Either[? <: Throwable, A]

object ValidatedEither:
  given validatedNecEither[A]: ValidatedEither[A] with
    extension (maybeA: AllErrorsOr[A])
      def either: Either[? <: Throwable, A] = maybeA match
        case Validated.Valid(value) => Right(value)
        case Validated.Invalid(errors) => Left(ValidationErrors(errors))
