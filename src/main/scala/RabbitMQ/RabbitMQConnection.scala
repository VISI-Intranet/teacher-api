package RabbitMQ
import com.rabbitmq.client.{ConnectionFactory, Connection, Channel}

object RabbitMQConnection {
  def createConnection(): (Connection, Channel) = {
    val factory = new ConnectionFactory()
    factory.setHost("localhost")
    factory.setUsername("user")
    factory.setPassword("user")

    val connection = factory.newConnection()
    val channel = connection.createChannel()

    (connection, channel)
  }
}