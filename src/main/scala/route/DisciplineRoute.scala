package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import com.rabbitmq.client.{ConnectionFactory, MessageProperties}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import repository.{DisciplineRepository, TeacherRepository}
import model.Discipline

import java.nio.charset.StandardCharsets
import scala.util.{Failure, Success}
import RabbitMQ.{RabbitMQConnection, RabbitMQProducer}
import RabbitMQ.RabbitMQConnection.createConnection

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

  val (connection, channel) = createConnection()

  val queueName = "DisciplineQueue"

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

                  onSuccess(DisciplineRepository.addDiscipline(discipline)) { result =>

                    RabbitMQProducer.publishMessage("DisciplineQueue","DisciplineExchange" , "DisciplinePutRoutingKey" ,serialization.write(discipline))

                    complete(result)
                  }
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
                    case Success(result) =>
                      channel.queueDeclare("DisciplineQueue", true, false, false, null)
                      val message = serialization.write(updatedDiscipline)
                      channel.basicPublish("DisciplineExchange", "DisciplineRoutingKey", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8))
                      println(s" [x] Sent '$message'")
                      complete("Update successful") // Измените на ваш ответ при необходимости

                    case Failure(ex) =>
                      println(s"Update failed: ${ex.getMessage}")
                      complete(StatusCodes.InternalServerError, s"Update failed: ${ex.getMessage}")
                  }
                  //                  onComplete(DisciplineRepository.updateDiscipline(idAsInt, updatedDiscipline)) {
                  //                    case Success(rowsAffected) =>
                  //                      if (rowsAffected > 0) {
                  //                        complete(s"Rows affected: $rowsAffected")
                  //                      } else {
                  //                        complete(404, "Discipline not found")
                  //                      }
                  //                    case Failure(ex) =>
                  //                      complete(500, s"Update failed: ${ex.getMessage}")
                  //                  }
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
