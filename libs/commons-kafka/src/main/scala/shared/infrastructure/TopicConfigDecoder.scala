package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaConfig.Topic

import ciris.{ConfigDecoder, ConfigError}

trait TopicConfigDecoder:
  implicit def topicConfigDecoder: ConfigDecoder[String, Topic] =
    ConfigDecoder.lift(string =>
      Topic.from(string) match
        case Some(value) => Right(value)
        case None => Left(ConfigError("Invalid topic")),
    )
