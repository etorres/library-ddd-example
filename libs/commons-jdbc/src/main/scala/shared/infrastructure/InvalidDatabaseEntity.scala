package es.eriktorr.library
package shared.infrastructure

import shared.ValidationError

final case class InvalidDatabaseEntity(message: String) extends ValidationError(message)
