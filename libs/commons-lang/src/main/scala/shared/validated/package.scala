package es.eriktorr.library
package shared

import shared.ValidationError

import cats.data.ValidatedNec

package object validated:
  type AllErrorsOr[A] = ValidatedNec[ValidationError, A]
