package repository

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import model.{Student, TeacherDiscipline}
import Connection.PostgresConnection
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.DefaultFormats

object TeacherDisciplineRepository {

  class TeacherDisciplines(tag: Tag) extends Table[TeacherDiscipline](tag, "teacher_discipline") {
    def teacher_id = column[Option[Int]]("teacher_id")
    def discipline_id = column[Option[Int]]("discipline_id")

    def * = (teacher_id, discipline_id).mapTo[TeacherDiscipline]
  }

  val teacherDisciplines = TableQuery[TeacherDisciplines]

  def getAllTeacherDisciplines(): Future[Seq[TeacherDiscipline]] =
    PostgresConnection.db.run(teacherDisciplines.result)

  def getTeacherDisciplineById(id: Int): Future[Option[TeacherDiscipline]] =
    PostgresConnection.db.run(teacherDisciplines.filter(_.teacher_id === id).result.headOption)

  def addTeacherDiscipline(teacherDiscipline: TeacherDiscipline): Future[Option[Int]] =
    PostgresConnection.db.run(teacherDisciplines returning teacherDisciplines.map(_.teacher_id) += teacherDiscipline)


  def updateTeacherDiscipline(id: Int, updatedTeacherDiscipline: TeacherDiscipline): Future[Int] = {
    val updatedTeacherDisciplineWithId = updatedTeacherDiscipline.copy(teacherId = Some(id))

    PostgresConnection.db.run(teacherDisciplines.filter(_.teacher_id === id).update(updatedTeacherDiscipline)).map { rowsAffected =>
      println(s"Rows affected: $rowsAffected")
      rowsAffected
    }.recover {
      case ex =>
        println(s"Update failed: ${ex.getMessage}")
        throw ex
    }
  }


  def deleteTeacherDiscipline(id: Int): Future[Int] =
    PostgresConnection.db.run(teacherDisciplines.filter(_.teacher_id === id).delete)
}
