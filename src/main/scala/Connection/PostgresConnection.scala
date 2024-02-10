package Connection

import slick.jdbc.JdbcBackend.Database


object PostgresConnection {
  val db: Database = Database.forURL(
    url = "jdbc:postgresql://localhost:5432/univer",
    user = "admin",
    password = "123",
    driver = "org.postgresql.Driver"
  )
}
