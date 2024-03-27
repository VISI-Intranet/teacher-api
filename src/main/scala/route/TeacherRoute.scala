package route

import io.circe.parser.decode
import io.circe.generic.auto._
import Alpakka.Operations.{SendMessageAndWaitForResponsAlpakka, SendMessageWithCorrelationIdAlpakka}
import Alpakka.RabbitMQModel.RabbitMQModel
import RabbitMQ.RabbitMQOperation.Operations.Formatter.{extractContent, extractContentList}
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.stream.alpakka.amqp.AmqpConnectionProvider
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import model.Teacher
import model.importModel.{CreateUserCommand, ImportDocumentation}
import org.json4s.{DefaultFormats, jackson}
import repository._

import scala.concurrent.{ExecutionContext, Future}


class TeacherRoutes(amqpConnectionProvider: AmqpConnectionProvider) extends Json4sSupport {

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats
  implicit lazy val system: ActorSystem = ActorSystem("web-system")
  implicit lazy val mat: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global


  val pubMQModel: RabbitMQModel = RabbitMQModel("TeacherPublisher", "UniverSystem", "univer.teacher-api.studentsByIdGet")
  val replyMQModel: RabbitMQModel = RabbitMQModel("TeacherSubscription", "UniverSystem", "univer.student-api.studentsByIdGet")
  val pubLibUserMQModel: RabbitMQModel = RabbitMQModel("TeacherPublisher", "UniverSystem", "univer.teacher-api.createUserPost")

  val pubTeacherGradeMQModel: RabbitMQModel = RabbitMQModel("TeacherPublisher", "UniverSystem", "univer.teacher-api.studentsGradeByIdGet")
  val replyTeacherGradeMQModel: RabbitMQModel = RabbitMQModel("TeacherSubscription", "UniverSystem", "univer.student-api.studentsGradeByIdGet")


  val route =
    pathPrefix("teacher") {
      concat(
        pathEnd {
          concat(
            get {
              complete(TeacherRepository.getAllTeachers())
            },
            post {
              entity(as[Teacher]) { teacher =>

                val addTeacherOperation = TeacherRepository.addTeacher(teacher)

                val sendMessageOperation = addTeacherOperation.flatMap { _ =>

                  val c = CreateUserCommand(_id = teacher._id, name = teacher.name, age = teacher.age, email = teacher.email, phoneNumber = teacher.phoneNumber, checkCreate = true, userType = "TeacherUser")
                  SendMessageWithCorrelationIdAlpakka.sendMessageWithCorrelationId(extractContent(c), pubLibUserMQModel, amqpConnectionProvider)()
                }

                val result = sendMessageOperation.recoverWith { case ex =>
                  println(s"Failed to add teacher: ${ex.getMessage}")
                  Future.successful(())
                }

                onComplete(result) { _ =>
                  complete("Teacher added successfully")
                }

              }

            }
          )
        },
        path(Segment) { teacherId =>
          concat(
            get {
              complete(TeacherRepository.getTeacherById(teacherId))
            },
            put {
              entity(as[Teacher]) { updatedTeacher =>
                complete(TeacherRepository.updateTeacher(teacherId, updatedTeacher))
              }
            },
            delete {
              complete(TeacherRepository.deleteTeacher(teacherId))
            }
          )

        }

      )
    } ~
      pathPrefix("teacherDocument") {
        path(Segment) { teacherId =>
          concat(
            get {
              val teacherFuture: Future[Option[Teacher]] = TeacherRepository.getTeacherById(teacherId)
              val resultFuture: Future[Option[Teacher]] = teacherFuture.flatMap {
                case Some(teacher) =>
                  val sendResultFuture = SendMessageAndWaitForResponsAlpakka.sendMessageAndWaitForResponse(extractContentList(teacher.disciplinesId), pubMQModel, replyMQModel, amqpConnectionProvider)()
                  sendResultFuture.map { result =>
                    val jsonObjects: List[String] = result.trim.split(":,:").toList.map(_.trim)
                    val importDocumentations = jsonObjects.flatMap(jsonString => decode[ImportDocumentation](jsonString).toOption)
                    val updatedTeacher = teacher.copy(documents = Option(importDocumentations))
                    Some(updatedTeacher)
                  }
                case None =>
                  Future.successful(None)
              }
              onSuccess(resultFuture) {
                case Some(teacher) => complete(teacher)
                case None => complete(StatusCodes.NotFound)
              }
            }
          )
        }
      } ~
      pathPrefix("teacherAverageGrade") {
        path(Segment) { teacherId =>
          concat(
            get {
              val teacherFuture: Future[Option[Teacher]] = TeacherRepository.getTeacherById(teacherId)
              val resultFuture: Future[Option[Teacher]] = teacherFuture.flatMap {
                case Some(teacher) =>
                  val sendResultFuture = SendMessageAndWaitForResponsAlpakka.sendMessageAndWaitForResponse(extractContentList(teacher.studentsId), pubTeacherGradeMQModel, replyTeacherGradeMQModel, amqpConnectionProvider)()
                  sendResultFuture.map { result =>


                    val keyValueStr = result.stripPrefix("HashMap(").stripSuffix(")")


                    val keyValuePairs = keyValueStr.split(", ")

                    // Создаем Map из пар ключ-значение
                    val resultMap = keyValuePairs.map { pairStr =>
                      val Array(key, value) = pairStr.split(" -> ")
                      key -> value.toInt
                    }.toMap

                    println("resultmap : " + resultMap)

                    val updatedTeacher = teacher.copy(studentsAverage = Option(resultMap))
                    println(updatedTeacher)
                    Some(updatedTeacher)
                  }

              }
              onSuccess(resultFuture) {
                case Some(teacher) => complete(teacher)
              }
            }
          )
        }
      }


}

object TeacherRoutes {
  def apply(amqpConnectionProvider: AmqpConnectionProvider): TeacherRoutes = new TeacherRoutes(amqpConnectionProvider)
}
