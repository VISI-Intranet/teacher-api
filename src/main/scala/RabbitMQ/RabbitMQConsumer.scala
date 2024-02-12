package RabbitMQ

import RabbitMQ.RabbitMQConnection.createConnection
import com.rabbitmq.client.{AMQP, DefaultConsumer, Envelope}

object RabbitMQConsumer {

  def ConsumerListenFunction(QUEUE_NAME:String):Unit = {

    val (connection, channel) = createConnection()

    // Объявление очереди
    channel.queueDeclare(QUEUE_NAME, true, false, false, null)

    // Создание объекта консьюмера
    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val message = new String(body, "UTF-8")
        println(s"Received message: $message")
      }
    }

    // Начало прослушивания очереди
    channel.basicConsume(QUEUE_NAME, true, consumer)
  }



}
