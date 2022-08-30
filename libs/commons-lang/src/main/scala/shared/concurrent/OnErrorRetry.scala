package es.eriktorr.library
package shared.concurrent

import cats.effect.Temporal
import cats.syntax.all.*
import cats.{ApplicativeError, Defer, MonadError}

import scala.concurrent.duration.{Duration, FiniteDuration}

/** Provides a simple retry with exponential backoff.
  *
  * @see
  *   [[https://alexn.org/blog/2020/08/03/on-error-retry-loop/ Retry Failing Tasks with Cats and Scala]]
  */
object OnErrorRetry:

  final case class RetryConfig(
      maxRetries: Int,
      initialDelay: FiniteDuration,
      maxDelay: FiniteDuration,
      backoffFactor: Double,
      private val evolvedDelay: Option[FiniteDuration] = None,
  ):
    def canRetry: Boolean = maxRetries > 0

    def delay: FiniteDuration = evolvedDelay.getOrElse(initialDelay)

    def evolve: RetryConfig = copy(
      maxRetries = math.max(maxRetries - 1, 0),
      evolvedDelay = Some {
        val nextDelay = evolvedDelay.getOrElse(initialDelay) * backoffFactor
        maxDelay.min(nextDelay) match
          case ref: FiniteDuration => ref
          case _: Duration.Infinite => maxDelay
      },
    )

  enum RetryOutcome:
    case Next extends RetryOutcome
    case Raise extends RetryOutcome

  private[this] def loop[F[_], A, S](fa: F[A], initial: S)(
      f: (Throwable, S, S => F[A]) => F[A],
  )(implicit F: ApplicativeError[F, Throwable], D: Defer[F]): F[A] = fa.handleErrorWith { err =>
    f(err, initial, state => D.defer(loop(fa, state)(f)))
  }

  def withBackoff[F[_], A](fa: F[A], config: RetryConfig)(
      p: Throwable => F[RetryOutcome],
  )(implicit F: MonadError[F, Throwable], D: Defer[F], timer: Temporal[F]): F[A] =
    OnErrorRetry.loop(fa, config) { (error, state, retry) =>
      if state.canRetry then
        p(error).flatMap {
          case RetryOutcome.Next => timer.sleep(state.delay) *> retry(state.evolve)
          case RetryOutcome.Raise => F.raiseError(error)
        }
      else F.raiseError(error)
    }
