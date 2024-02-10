package route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import repository._
import model._

import repository._

import scala.util.{Failure, Success}

object TeacherRoutes extends Json4sSupport {

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
      pathPrefix("teacher") {
        concat(
          pathEnd {
            concat(
              get {
                complete(TeacherRepository.getAllTeachers())
              },
              post {
                entity(as[Teacher]) { teacher =>
                  complete(TeacherRepository.addTeacher(teacher))
                }
              }
            )
          },
          path(Segment) { teacherId =>
            val idAsInt = teacherId.toInt
            concat(
              get {
                complete(TeacherRepository.getTeacherById(idAsInt))
              },
              put {
                entity(as[Teacher]) { updatedTeacher =>
                  onComplete(TeacherRepository.updateTeacher(idAsInt, updatedTeacher)) {
                    case Success(rowsAffected) =>
                      if (rowsAffected > 0) {
                        complete(s"Rows affected: $rowsAffected")
                      } else {
                        complete(404, "Teacher not found")
                      }
                    case Failure(ex) =>
                      complete(500, s"Update failed: ${ex.getMessage}")
                  }
                }
              },
              delete {
                onComplete(TeacherRepository.deleteTeacher(idAsInt)) {
                  case Success(rowsAffected) =>
                    if (rowsAffected > 0) {
                      complete(s"Rows affected: $rowsAffected")
                    } else {
                      complete(404, "Teacher not found")
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
