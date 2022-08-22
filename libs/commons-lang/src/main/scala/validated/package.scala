package es.eriktorr.library

import cats.data.{Validated, ValidatedNec}
import cats.effect.IO

package object validated:
  type AllErrorsOr[A] = ValidatedNec[ValidationError, A]
