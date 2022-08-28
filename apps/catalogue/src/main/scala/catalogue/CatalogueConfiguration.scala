package es.eriktorr.library
package catalogue

import shared.infrastructure.JdbcConfig

final case class CatalogueConfiguration(jdbcConfig: JdbcConfig):
    def asString: String = s"${jdbcConfig.asString}"
