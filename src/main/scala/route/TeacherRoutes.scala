package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{DefaultFormats, jackson}
import repository._
import model._
import scala.util.{Failure, Success}
import com.rabbitmq.client.{ConnectionFactory, MessageProperties}


import java.nio.charset.StandardCharsets

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

  val factory = new ConnectionFactory()
  factory.setHost("localhost") // Укажите ваш хост RabbitMQ
  factory.setUsername("user") // Укажите ваше имя пользователя RabbitMQ
  factory.setPassword("user") // Укажите ваш пароль RabbitMQ


  val connection = factory.newConnection()
  val channel = connection.createChannel()

  val queueName = "DisciplineQueue"



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

                  onSuccess(TeacherRepository.addTeacher(teacher)) { result =>
                    channel.queueDeclare("DisciplineQueue", true, false, false, null)
                    val message = serialization.write(teacher)
                    channel.basicPublish("DisciplineExchange", "TeacherRoutingKey", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8))
                    println(s" [x] Sent '$message'")

                    complete(result) // Respond with the result if necessary
                  }
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
                    case Success(result) =>
                      channel.queueDeclare("DisciplineQueue", true, false, false, null)
                      val message = serialization.write(updatedTeacher)
                      channel.basicPublish("DisciplineExchange", "TeacherRoutingKey", MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8))
                      println(s" [x] Sent '$message'")
                      complete("Update successful") // Измените на ваш ответ при необходимости

                    case Failure(ex) =>
                      println(s"Update failed: ${ex.getMessage}")
                      complete(StatusCodes.InternalServerError, s"Update failed: ${ex.getMessage}")
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
