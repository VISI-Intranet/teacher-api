package repository

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import model.Student
import Connection.PostgresConnection
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.DefaultFormats

object StudentRepository {

  class Students(tag: Tag) extends Table[Student](tag, "students") {
    def studentId = column[Option[Int]]("student_id", O.PrimaryKey, O.AutoInc)
    def name = column[Option[String]]("student_name")

    def * = (studentId, name).mapTo[Student]
  }

  val students = TableQuery[Students]

  def getAllStudents(): Future[Seq[Student]] =
    PostgresConnection.db.run(students.result)

  def getStudentById(id: Int): Future[Option[Student]] =
    PostgresConnection.db.run(students.filter(_.studentId === id).result.headOption)

  def addStudent(student: Student): Future[Option[Int]] =
    PostgresConnection.db.run(students returning students.map(_.studentId) += student)

  def updateStudent(id: Int, updatedStudent: Student): Future[Int] = {
    val updatedDisciplineWithId = updatedStudent.copy(studentId = Some(id))

    PostgresConnection.db.run(students.filter(_.studentId === id).update(updatedDisciplineWithId)).map { rowsAffected =>
      println(s"Rows affected: $rowsAffected")
      rowsAffected
    }.recover {
      case ex =>
        println(s"Update failed: ${ex.getMessage}")
        throw ex
    }
  }

  def deleteStudent(id: Int): Future[Int] =
    PostgresConnection.db.run(students.filter(_.studentId === id).delete)
}
