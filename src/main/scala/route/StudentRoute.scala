package route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import repository.StudentRepository
import model.Student

import scala.util.{Failure, Success}

object StudentRoutes extends Json4sSupport {

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats

  // Обработчик исключений для получения деталей об ошибке
  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ex: Throwable =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally. Exception: ${ex.getMessage}")
        complete(500, "Internal Server Error")
      }
  }

  val route =
    handleExceptions(exceptionHandler) {
      pathPrefix("students") {
        concat(
          pathEnd {
            concat(
              get {
                complete(StudentRepository.getAllStudents())
              },
              post {
                entity(as[Student]) { student =>
                  complete(StudentRepository.addStudent(student))
                }
              }
            )
          },
          path(Segment) { studentId =>
            val idAsInt = studentId.toInt
            concat(
              get {
                complete(StudentRepository.getStudentById(idAsInt))
              },
              put {
                entity(as[Student]) { updatedStudent =>
                  onComplete(StudentRepository.updateStudent(idAsInt, updatedStudent)) {
                    case Success(rowsAffected) =>
                      if (rowsAffected > 0) {
                        complete(s"Rows affected: $rowsAffected")
                      } else {
                        complete(404, "Student not found")
                      }
                    case Failure(ex) =>
                      complete(500, s"Update failed: ${ex.getMessage}")
                  }
                }
              },
              delete {
                onComplete(StudentRepository.deleteStudent(idAsInt)) {
                  case Success(rowsAffected) =>
                    if (rowsAffected > 0) {
                      complete(s"Rows affected: $rowsAffected")
                    } else {
                      complete(404, "Student not found")
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

