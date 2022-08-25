package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaConfig.SchemaRegistry

import ciris.{ConfigDecoder, ConfigError}

trait SchemaRegistryConfigDecoder:
  implicit def schemaRegistryConfigDecoder: ConfigDecoder[String, SchemaRegistry] =
    ConfigDecoder.lift(string =>
      SchemaRegistry.from(string) match
        case Some(value) => Right(value)
        case None => Left(ConfigError("Invalid schema registry")),
    )
