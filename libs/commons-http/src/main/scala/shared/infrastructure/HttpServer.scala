package es.eriktorr.library
package shared.infrastructure

import cats.effect.IO
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{CORS, GZip, Logger as Http4sLogger}
import org.http4s.{HttpApp, Method}

import scala.concurrent.duration.*

object HttpServer:
  def httpServiceUsing(
      httpServerConfig: HttpServerConfig,
      httpApp: HttpApp[IO],
      enableLogs: Boolean = false,
  ): IO[Unit] =
    val enhancedHttpApp = Http4sLogger.httpApp(logHeaders = enableLogs, logBody = enableLogs)(
      CORS.policy.withAllowOriginAll
        .withAllowMethodsIn(Set(Method.GET, Method.POST))
        .withAllowCredentials(false)
        .withMaxAge(1.day)
        .apply(GZip(httpApp)),
    )

    EmberServerBuilder
      .default[IO]
      .withHost(httpServerConfig.host)
      .withPort(httpServerConfig.port)
      .withHttpApp(enhancedHttpApp)
      .build
      .use(_ => IO.never)
