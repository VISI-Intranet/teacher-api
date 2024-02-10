package repository
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import model.TeacherStudent
import Connection.PostgresConnection

object TeacherStudentRepository {

  class TeacherStudents(tag: Tag) extends Table[TeacherStudent](tag, "teacher_student") {
    def teacher_id = column[Option[Int]]("teacher_id")
    def student_id = column[Option[Int]]("student_id")

    def * = (teacher_id, student_id).mapTo[TeacherStudent]
  }

  val teacherStudents = TableQuery[TeacherStudents]

  def getAllTeacherStudents(): Future[Seq[TeacherStudent]] =
    PostgresConnection.db.run(teacherStudents.result)

  def getTeacherStudentById(teacherId: Int, studentId: Int): Future[Option[TeacherStudent]] =
    PostgresConnection.db.run(teacherStudents.filter(ts => ts.teacher_id === teacherId && ts.student_id === studentId).result.headOption)

  def addTeacherStudent(teacherStudent: TeacherStudent): Future[Option[Int]] =
    PostgresConnection.db.run(teacherStudents returning teacherStudents.map(_.teacher_id) += teacherStudent)

  def updateTeacherStudent(teacherId: Int, studentId: Int, updatedTeacherStudent: TeacherStudent): Future[Int] = {
    val updatedTeacherStudentWithId = updatedTeacherStudent.copy(teacherId = Some(teacherId), studentId = Some(studentId))

    PostgresConnection.db.run(teacherStudents.filter(ts => ts.teacher_id === teacherId && ts.student_id === studentId).update(updatedTeacherStudentWithId)).map { rowsAffected =>
      println(s"Rows affected: $rowsAffected")
      rowsAffected
    }.recover {
      case ex =>
        println(s"Update failed: ${ex.getMessage}")
        throw ex
    }
  }

  def deleteTeacherStudent(teacherId: Int, studentId: Int): Future[Int] =
    PostgresConnection.db.run(teacherStudents.filter(ts => ts.teacher_id === teacherId && ts.student_id === studentId).delete)
}

