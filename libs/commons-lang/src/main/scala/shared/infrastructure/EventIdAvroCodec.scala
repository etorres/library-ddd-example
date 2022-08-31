package es.eriktorr.library
package shared.infrastructure

import shared.EventId

import cats.syntax.either.*
import vulcan.{AvroError, Codec}

trait EventIdAvroCodec:
  implicit val eventIdAvroCodec: Codec[EventId] = Codec.uuid.imap(EventId.from)(_.value)
