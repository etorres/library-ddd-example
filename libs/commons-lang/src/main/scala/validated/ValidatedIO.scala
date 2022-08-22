package es.eriktorr.library
package validated

import cats.data.Validated
import cats.effect.IO

trait ValidatedIO[A]:
  def validated: IO[A]

object ValidatedIO:
  given allErrorsValidatedIO[A](using validatedIO: ValidatedIO[A]): ValidatedIO[AllErrorsOr[A]] with
    def validated: IO[AllErrorsOr[A]] = ???

/*
trait Ord[T]:
  def compare(x: T, y: T): Int
  extension (x: T) def < (y: T) = compare(x, y) < 0
  extension (x: T) def > (y: T) = compare(x, y) > 0

given intOrd: Ord[Int] with
  def compare(x: Int, y: Int) =
    if x < y then -1 else if x > y then +1 else 0

given listOrd[T](using ord: Ord[T]): Ord[List[T]] with

  def compare(xs: List[T], ys: List[T]): Int = (xs, ys) match
    case (Nil, Nil) => 0
    case (Nil, _) => -1
    case (_, Nil) => +1
    case (x :: xs1, y :: ys1) =>
      val fst = ord.compare(x, y)
      if fst != 0 then fst else compare(xs1, ys1)
 */

/*
trait AllErrorsOrSyntax:
  implicit class AllErrorsOrOps[A](self: AllErrorsOr[A]):
    def validate: IO[A] = self match
      case Validated.Valid(value) => IO.pure(value)
      case Validated.Invalid(errors) => IO.raiseError(ValidationErrors(errors))

object AllErrorsOr:
  given AllErrorsOrOps[A] = ???
 */
