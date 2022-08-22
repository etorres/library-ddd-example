package es.eriktorr.library

abstract class ValidationError(message: String, cause: Option[Throwable] = Option.empty[Throwable])
    extends DomainError(message, cause)
