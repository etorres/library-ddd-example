package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.JdbcTestConfig.{
  postgresqlDriverClassName,
  testConnectUrlFrom,
  testPassword,
  testUser,
}
import shared.refined.types.NonEmptyString

import ciris.Secret

enum JdbcTestConfig(val jdbcConfig: JdbcConfig, val schema: NonEmptyString):
  case Catalogue
      extends JdbcTestConfig(
        JdbcConfig(
          JdbcTestConfig.postgresqlDriverClassName,
          JdbcTestConfig.testConnectUrlFrom("catalogue"),
          JdbcTestConfig.testUser("catalogue"),
          JdbcTestConfig.testPassword,
        ),
        NonEmptyString.unsafeFrom("test_catalogue"),
      )
  case LendingBooks
      extends JdbcTestConfig(
        JdbcConfig(
          JdbcTestConfig.postgresqlDriverClassName,
          JdbcTestConfig.testConnectUrlFrom("lending"),
          JdbcTestConfig.testUser("lending"),
          JdbcTestConfig.testPassword,
        ),
        NonEmptyString.unsafeFrom("test_lending_books"),
      )
  case LendingPatrons
      extends JdbcTestConfig(
        JdbcConfig(
          JdbcTestConfig.postgresqlDriverClassName,
          JdbcTestConfig.testConnectUrlFrom("lending"),
          JdbcTestConfig.testUser("lending"),
          JdbcTestConfig.testPassword,
        ),
        NonEmptyString.unsafeFrom("test_lending_patrons"),
      )

object JdbcTestConfig:
  final private lazy val postgresqlDriverClassName =
    NonEmptyString.unsafeFrom("org.postgresql.Driver")
  final private def testConnectUrlFrom(name: String) =
    NonEmptyString.unsafeFrom(s"jdbc:postgresql://localhost:5432/${name}_db")
  final private lazy val testPassword = Secret(NonEmptyString.unsafeFrom("changeme"))
  final private def testUser(name: String) = NonEmptyString.unsafeFrom(s"${name}_user")
