package repository

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Connection.PostgresConnection
import model.Discipline
import scala.util.Try;

object DisciplineRepository {

  class Disciplines(tag: Tag) extends Table[Discipline](tag, "discipline") {
    def disciplineId = column[Option[Int]]("discipline_id", O.PrimaryKey, O.AutoInc)
    def disciplineName = column[Option[String]]("discipline_name")

    def * = (disciplineId, disciplineName).mapTo[Discipline]
  }

  val disciplines = TableQuery[Disciplines]

  def getAllDisciplines(): Future[Seq[Discipline]] =
    PostgresConnection.db.run(disciplines.result)

  def getFilteredDisciplinesByName(name:String): Future[Seq[Discipline]] = {
    PostgresConnection.db.run(disciplines.filter(_.disciplineName === name).result)

  }

  def getDisciplineById(id: Int): Future[Option[Discipline]] =
    PostgresConnection.db.run(disciplines.filter(_.disciplineId === id).result.headOption)

  def addDiscipline(discipline: Discipline): Future[Option[Int]] =
    PostgresConnection.db.run(disciplines returning disciplines.map(_.disciplineId) += discipline)

  def updateDiscipline(id: Int, updatedDiscipline: Discipline): Future[Int] = {
    val updatedDisciplineWithId = updatedDiscipline.copy(disciplineId = Some(id))

    PostgresConnection.db.run(disciplines.filter(_.disciplineId === id).update(updatedDisciplineWithId)).map { rowsAffected =>
      println(s"Rows affected: $rowsAffected")
      rowsAffected
    }.recover {
      case ex =>
        println(s"Update failed: ${ex.getMessage}")
        throw ex
    }
  }


  def deleteDiscipline(id: Int): Future[Int] =
    PostgresConnection.db.run(disciplines.filter(_.disciplineId === id).delete)
}
