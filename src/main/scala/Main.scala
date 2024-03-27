import Alpakka.RabbitMQModel.RabbitMQModel

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.stream.alpakka.amqp.{AmqpConnectionProvider, AmqpLocalConnectionProvider}
import route._

object Main extends App {




  implicit val system: ActorSystem = ActorSystem("web-service")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  val pubMQModel: RabbitMQModel = RabbitMQModel("TeacherPublisher", "UniverSystem", "univer.teacher-api.studentsByIdGet")
  val replyMQModel: RabbitMQModel = RabbitMQModel("TeacherSubscription", "UniverSystem", "univer.student-api.studentsByIdGet")

  val amqpConnectionProvider :AmqpConnectionProvider = AmqpLocalConnectionProvider

  val routes = TeacherRoutes(amqpConnectionProvider).route

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8081)

  println("Server is online at http://localhost:8082/\nPress RETURN to stop...")

  StdIn.readLine()

  bindingFuture
    .flatMap(_ => bindingFuture.flatMap(_.unbind()))
    .onComplete(_ => {
      system.terminate()
    })



}