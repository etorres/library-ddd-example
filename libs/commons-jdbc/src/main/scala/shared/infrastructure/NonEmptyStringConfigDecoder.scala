package es.eriktorr.library
package shared.infrastructure

import shared.refined.types.NonEmptyString

import cats.data.Validated
import ciris.{ConfigDecoder, ConfigError}

trait NonEmptyStringConfigDecoder:
  implicit def nonEmptyStringServerConfigDecoder: ConfigDecoder[String, NonEmptyString] =
    ConfigDecoder.lift(string =>
      NonEmptyString.from(string) match
        case Validated.Valid(value) => Right(value)
        case Validated.Invalid(_) => Left(ConfigError("Invalid bootstrap server")),
    )
