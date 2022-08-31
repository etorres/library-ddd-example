package es.eriktorr.library
package book.infrastructure

import book.model.BookType
import shared.Namespaces

import cats.syntax.either.*
import vulcan.{AvroError, Codec}

import scala.util.Try

trait BookTypeAvroCodec:
  implicit val bookTypeAvroCodec: Codec[BookType] = Codec.enumeration[BookType](
    name = "BookType",
    namespace = Namespaces.default,
    doc = Some("All possible states of a book instance"),
    symbols = BookType.values.toList.map(_.toString),
    encode = (bookType: BookType) => bookType.toString,
    decode = (value: String) =>
      Try(BookType.valueOf(value)).toEither.leftMap(_ => AvroError(s"$value is not a BookType")),
    default = Some(BookType.Restricted),
  )
