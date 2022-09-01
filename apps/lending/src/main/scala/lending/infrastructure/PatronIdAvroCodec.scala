package es.eriktorr.library
package lending.infrastructure

import lending.model.PatronId

import vulcan.Codec

trait PatronIdAvroCodec:
  implicit val patronIdAvroCodec: Codec[PatronId] = Codec.uuid.imap(PatronId.from)(_.value)
