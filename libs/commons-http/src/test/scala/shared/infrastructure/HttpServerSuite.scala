package es.eriktorr.library
package shared.infrastructure

import shared.RESTful.apiRootPath

import cats.effect.IO
import cats.syntax.traverse.*
import io.circe.Encoder
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.http4s.circe.jsonEncoderOf
import org.http4s.{EntityDecoder, HttpApp, Method, Request, Status, Uri}
import org.scalacheck.Test

trait HttpServerSuite extends CatsEffectSuite with ScalaCheckEffectSuite:
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

  def requestFrom[A](uri: String, a: A)(using encoderA: Encoder[A]): Request[IO] = Request(
    method = Method.POST,
    uri = Uri.unsafeFromString(s"$apiRootPath/$uri"),
    body = jsonEncoderOf[IO, A].toEntity(a).body,
  )
