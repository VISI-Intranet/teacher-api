package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import repository.{DisciplineRepository, TeacherRepository}
import model.Discipline

import java.nio.charset.StandardCharsets
import scala.util.{Failure, Success}

object DisciplineRoutes extends Json4sSupport {

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
      pathPrefix("discipline") {
        concat(
          get {
            parameters("name") { name =>
              onSuccess(DisciplineRepository.getFilteredDisciplinesByName(name)) { filteredDisciplines =>
                complete(filteredDisciplines)
              }
            }
          },
          pathEnd {
            concat(
              get {
                complete(DisciplineRepository.getAllDisciplines())
              },
              post {
                entity(as[Discipline]) { discipline =>
                    complete(DisciplineRepository.addDiscipline(discipline)) // Respond with the result if necessary
                  }
              }
            )
          },
          path(Segment) { disciplineId =>
            val idAsInt = disciplineId.toInt
            concat(
              get {
                complete(DisciplineRepository.getDisciplineById(idAsInt))
              },
              put {
                entity(as[Discipline]) { updatedDiscipline =>

                                    onComplete(DisciplineRepository.updateDiscipline(idAsInt, updatedDiscipline)) {
                                      case Success(rowsAffected) =>
                                        if (rowsAffected > 0) {
                                          complete(s"Rows affected: $rowsAffected")
                                        } else {
                                          complete(404, "Discipline not found")
                                        }
                                      case Failure(ex) =>
                                        complete(500, s"Update failed: ${ex.getMessage}")
                                    }
                }
              },
              delete {
                onComplete(DisciplineRepository.deleteDiscipline(idAsInt)) {
                  case Success(rowsAffected) =>
                    if (rowsAffected > 0) {
                      complete(s"Rows affected: $rowsAffected")
                    } else {
                      complete(404, "Discipline not found")
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
