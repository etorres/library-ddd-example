package es.eriktorr.library
package shared.infrastructure

import cats.effect.IO
import cats.syntax.traverse.*
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.http4s.{EntityDecoder, HttpApp, Request, Status}
import org.scalacheck.Test

final class HttpServerSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
  def checkUsing[A](
      httpApp: HttpApp[IO],
      request: Request[IO],
      expectedStatus: Status,
      expectedBody: Option[A],
  )(using entityDecoder: EntityDecoder[IO, A]): IO[Unit] = for
    response <- httpApp.run(request)
    body <- expectedBody.map(_ => response.as[A]).traverse(identity)
  yield
    assertEquals(response.status, expectedStatus)
    assertEquals(body, expectedBody)
