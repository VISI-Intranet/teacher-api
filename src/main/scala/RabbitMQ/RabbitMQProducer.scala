package RabbitMQ

import RabbitMQ.RabbitMQConnection.createConnection
import com.rabbitmq.client.MessageProperties
import org.json4s.jackson

object RabbitMQProducer {
  def publishMessage(queueName: String, exchangeName: String, routingKey: String, message: String): Unit = {
    val (connection, channel) = createConnection()

    try {
      // Объявление очереди
      channel.queueDeclare(queueName, true, false, false, null)
      // Опционально: Объявление Exchange и настройка маршрутизации
      // channel.exchangeDeclare(exchangeName, "direct", true)
      // channel.queueBind(queueName, exchangeName, routingKey)

      // Отправка сообщения в очередь
      channel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"))

      println(s" [x] Sent '$message'")
    } finally {
      // Закрытие соединения в блоке finally, чтобы гарантировать закрытие даже при возникновении исключения
      channel.close()
      connection.close()
    }
  }
}

