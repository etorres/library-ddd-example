package es.eriktorr.library
package infrastructure.jdbc

import ciris.Secret
import eu.timepit.refined.*
import eu.timepit.refined.cats.*
import eu.timepit.refined.predicates.all.*

object JdbcTestConfig:
    val jdbcConfig: JdbcConfig = JdbcConfig(
      refineMV[NonEmpty]("org.postgresql.Driver"),
      refineMV[NonEmpty]("jdbc:postgresql://localhost:5432/library_db"),
      refineMV[NonEmpty]("library_user"),
      Secret(refineMV[NonEmpty]("changeme"))
    )
