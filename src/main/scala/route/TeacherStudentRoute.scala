package route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import repository.TeacherStudentRepository
import model.TeacherStudent

import scala.util.{Failure, Success}

object TeacherStudentRoute extends Json4sSupport {

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  // Exception handler for getting details of the error
  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ex: Throwable =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally. Exception: ${ex.getMessage}")
        complete(500, "Internal Server Error")
      }
  }

  val route =
    handleExceptions(exceptionHandler) {
      pathPrefix("teacherStudent") {
        concat(
          pathEnd {
            concat(
              get {
                complete(TeacherStudentRepository.getAllTeacherStudents())
              },
              post {
                entity(as[TeacherStudent]) { teacherStudent =>
                  complete(TeacherStudentRepository.addTeacherStudent(teacherStudent))
                }
              }
            )
          },
          path(Segment / Segment) { (teacherId, studentId) =>
            val teacherIdAsInt = teacherId.toInt
            val studentIdAsInt = studentId.toInt
            concat(
              get {
                complete(TeacherStudentRepository.getTeacherStudentById(teacherIdAsInt, studentIdAsInt))
              },
              put {
                entity(as[TeacherStudent]) { updatedTeacherStudent =>
                  onComplete(TeacherStudentRepository.updateTeacherStudent(teacherIdAsInt, studentIdAsInt, updatedTeacherStudent)) {
                    case Success(rowsAffected) =>
                      if (rowsAffected > 0) {
                        complete(s"Rows affected: $rowsAffected")
                      } else {
                        complete(404, "TeacherStudent not found")
                      }
                    case Failure(ex) =>
                      complete(500, s"Update failed: ${ex.getMessage}")
                  }
                }
              },
              delete {
                onComplete(TeacherStudentRepository.deleteTeacherStudent(teacherIdAsInt, studentIdAsInt)) {
                  case Success(rowsAffected) =>
                    if (rowsAffected > 0) {
                      complete(s"Rows affected: $rowsAffected")
                    } else {
                      complete(404, "TeacherStudent not found")
                    }
                  case Failure(ex) =>
                    complete(500, s"Deletion failed: ${ex.getMessage}")
                }
              }
            )
          }
        )
      }
    }
}

