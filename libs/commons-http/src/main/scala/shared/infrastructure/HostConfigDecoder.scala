package es.eriktorr.library
package shared.infrastructure

import ciris.{ConfigDecoder, ConfigError}
import com.comcast.ip4s.Host

trait HostConfigDecoder:
  implicit def hostConfigDecoder: ConfigDecoder[String, Host] =
    ConfigDecoder.lift(host =>
      Host.fromString(host) match
        case Some(value) => Right(value)
        case None => Left(ConfigError("Invalid host")),
    )
