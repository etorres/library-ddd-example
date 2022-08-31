package es.eriktorr.library
package shared

sealed abstract class UUIDValidationError(
    message: String,
    cause: Option[Throwable] = Option.empty[Throwable],
) extends ValidationError(message, cause)

object UUIDValidationError:
  final case class UUIDInvalidFormat(cause: Throwable)
      extends UUIDValidationError("Invalid or unsupported UUID format", Some(cause))
