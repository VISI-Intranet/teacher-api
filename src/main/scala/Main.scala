import akka.actor.ActorSystem
import akka.actor.TypedActor.dispatcher
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import org.apache.kafka.clients.producer.KafkaProducer
import org.json4s.{DefaultFormats, jackson}
import route._


import java.util.Properties
import scala.io.StdIn;


object Main extends App {
  implicit val system = ActorSystem("professor-system")
  implicit val materializer = ActorMaterializer()

  val routes = TeacherRoutes.route ~
    StudentRoutes.route ~
    TeacherDisciplineRoute.route~
    TeacherStudentRoute.route ~
    DisciplineRoutes.route;

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8081);

  println(s"Server online at http://localhost:8081/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
