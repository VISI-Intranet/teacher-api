package Alpakka.Handlers.AddHandler

import Alpakka.Operations.SendMessageWithCorrelationIdAlpakka
import Alpakka.RabbitMQModel.RabbitMQModel
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpLocalConnectionProvider}
import repository.AdditionalRepo.Repo

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object RecieveHandler {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit lazy val system: ActorSystem = ActorSystem("web-system")
  implicit lazy val mat: Materializer = Materializer(system)

  val handler: (String, String) => Unit = (message, routingKey) => {

    routingKey match {
      case "univer.discipline-api.teacherForDisciplineByIdGet" =>

        val messageList = message.split(",").toList

        val replyTeacherToDisciplineMQModel: RabbitMQModel = RabbitMQModel("DisciplineSubscription", "UniverSystem", "univer.teacher-api.teacherForDisciplineByIdGet")
        val amqpConnectionProvider: AmqpConnectionProvider = AmqpLocalConnectionProvider

        println(messageList)
        val futureResult = Repo.getTeachersNamesByIds(messageList)

        futureResult.onComplete {
          case Success(result) =>
            println("Результат : " + result)

            SendMessageWithCorrelationIdAlpakka.sendMessageWithCorrelationId(result,replyTeacherToDisciplineMQModel,amqpConnectionProvider)()

          case Failure(exception) =>
            println(s"Произошла ошибка: ${exception.getMessage}")
        }

    println (message)
      case "key2" =>
        // Обработка для ключа "key2"
        println(s"Received message for key2: $message")
      case "univer.event-api.notficationEventForTeacherPost" =>
        println(message)
      case _ =>
        // Обработка для всех остальных случаев
        println(s"#")
    }

  }

}
