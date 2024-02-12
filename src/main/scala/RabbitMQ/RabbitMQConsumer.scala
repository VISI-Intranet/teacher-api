import RabbitMQ.RabbitMQConnection.createConnection
import RabbitMQ.RabbitMQProducer
import RabbitMQ.Repo.RabbitMQTeacherRepo
import RabbitMQ.Repo.RabbitMQTeacherRepo.convertMessageToIntList
import com.rabbitmq.client.{AMQP, DefaultConsumer, Envelope}
import org.json4s.{DefaultFormats, jackson}
import route.DisciplineRoutes.serialization

import scala.util.{Failure, Success}

object RabbitMQConsumer {

  def ConsumerListenFunction(QUEUE_NAME: String): Unit = {
    val (connection, channel) = createConnection()

    channel.queueDeclare(QUEUE_NAME, true, false, false, null)

    implicit val serialization = jackson.Serialization
    implicit val formats = DefaultFormats

    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(
                                   consumerTag: String,
                                   envelope: Envelope,
                                   properties: AMQP.BasicProperties,
                                   body: Array[Byte]
                                 ): Unit = {
        val message = new String(body, "UTF-8")
        println(s" [x] Received '$message'")
        val routingKey = envelope.getRoutingKey

        routingKey match {
          case "DisciplinePutRoutingKey" => {
            val asciiValues: List[Int] = convertMessageToIntList(message)

            import scala.concurrent.ExecutionContext.Implicits.global
            val futureResult = RabbitMQTeacherRepo.getTeachersByIds(asciiValues)

            futureResult.onComplete {
              case Success(result) =>
                RabbitMQProducer.publishMessage("TeacherQueue", "TeacherExchange", "TeacherPutRoutingQueue", serialization.write(result))

              case Failure(exception) =>
                println(s"Failed to retrieve teachers: ${exception.getMessage}")
              // Обработка ошибки
            }
          }
          case "TeacherRoutingKey" => {
            // Добавьте логику для обработки TeacherRoutingKey, если это необходимо
          }
        }
      }
    }

    channel.basicConsume(QUEUE_NAME, true, consumer)
  }
}
