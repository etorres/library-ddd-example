package es.eriktorr.library
package catalogue.integration

import infrastructure.jdbc.JdbcTransactorsSuite

final class JdbcCatalogueSuite extends JdbcTransactorsSuite:
    override def currentSchema: String = "test_catalogue_book"
