package util

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.concurrent.{ExecutionContext, Future}

object KafkaProducerUtil {

  private val kafkaProducerConfig = new Properties()
  kafkaProducerConfig.put("bootstrap.servers", "localhost:9092")
  kafkaProducerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  kafkaProducerConfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  private val kafkaProducer = new KafkaProducer[String, String](kafkaProducerConfig)

  def sendMessage(topic: String, message: String)(implicit ec: ExecutionContext): Future[Unit] = Future {
    val record = new ProducerRecord[String, String](topic, message)
    kafkaProducer.send(record)
  }
}
