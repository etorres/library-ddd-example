package es.eriktorr.library
package shared.infrastructure

import ciris.{ConfigDecoder, ConfigError}
import com.comcast.ip4s.Port

trait PortConfigDecoder:
  implicit def portConfigDecoder: ConfigDecoder[String, Port] = ConfigDecoder.lift(port =>
    Port.fromString(port) match
      case Some(value) => Right(value)
      case None => Left(ConfigError("Invalid port")),
  )
