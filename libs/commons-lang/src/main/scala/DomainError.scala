package es.eriktorr.library

import scala.util.control.NoStackTrace

@SuppressWarnings(Array("org.wartremover.warts.Null"))
abstract class DomainError(
    message: String,
    cause: Option[Throwable] = Option.empty[Throwable],
) extends NoStackTrace:
  import scala.language.unsafeNulls
  override def getCause: Throwable = cause.orNull
  override def getMessage: String = message
