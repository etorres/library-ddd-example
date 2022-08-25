package es.eriktorr.library
package shared

import cats.data.NonEmptyChain

final case class ValidationErrors(validationErrors: NonEmptyChain[ValidationError])
    extends ValidationError(
      s"One or more errors found: ${validationErrors.map(_.getMessage).toNonEmptyList.toList.mkString("\n")}",
    )
