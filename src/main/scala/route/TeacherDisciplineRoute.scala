package route


import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import repository.TeacherDisciplineRepository
import model.TeacherDiscipline

import scala.util.{Failure, Success}

object TeacherDisciplineRoute extends Json4sSupport {

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
      pathPrefix("teacherDiscipline") {
        concat(
          pathEnd {
            concat(
              get {
                complete(TeacherDisciplineRepository.getAllTeacherDisciplines())
              },
              post {
                entity(as[TeacherDiscipline]) { teacherDiscipline =>
                  complete(TeacherDisciplineRepository.addTeacherDiscipline(teacherDiscipline))
                }
              }
            )
          },
          path(Segment) { teacherDisciplineId =>
            val idAsInt = teacherDisciplineId.toInt
            concat(
              get {
                complete(TeacherDisciplineRepository.getTeacherDisciplineById(idAsInt))
              },
              put {
                entity(as[TeacherDiscipline]) { updatedTeacherDiscipline =>
                  onComplete(TeacherDisciplineRepository.updateTeacherDiscipline(idAsInt, updatedTeacherDiscipline)) {
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
                onComplete(TeacherDisciplineRepository.deleteTeacherDiscipline(idAsInt)) {
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

