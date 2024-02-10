package route

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import kafka.server.ConfigAdminManager.log
import model.Discipline
import org.apache.kafka.clients.producer.KafkaProducer
import org.json4s.{DefaultFormats, jackson}
import repository.DisciplineRepository
import util.KafkaProducerUtil

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object DisciplineRoutes extends Json4sSupport {

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats
  implicit val executor: ExecutionContext = scala.concurrent.ExecutionContext.global


  // Обработчик исключений для получения деталей об ошибке
  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case ex: Throwable =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally. Exception: ${ex.getMessage}")
        complete(500, "Internal Server Error")
      }
  }

  def route(kafkaProducer: KafkaProducer[String, String]): Route =
      pathPrefix("discipline") {
        concat(
          get {
            parameters("name") { name =>
              onSuccess(DisciplineRepository.getFilteredDisciplinesByName(name)) { filteredDisciplines =>
                complete(filteredDisciplines)
              }
            }
          },
          pathEnd {
            concat(
              get {
                complete(DisciplineRepository.getAllDisciplines())
              },
              post {
                entity(as[Discipline]) { discipline =>
                  log.info("Before sending message to Kafka")
                  val result = for {
                    _ <- DisciplineRepository.addDiscipline(discipline)
                    _ <- KafkaProducerUtil.sendMessage("discipline-topic", discipline.toString)
                  } yield ()
                  log.info("After sending message to Kafka")

                  onComplete(result) {
                    case Success(_) =>
                      log.info(s"Discipline added successfully. Message sent to Kafka: $discipline")
                      complete("Discipline added successfully")
                    case Failure(ex) =>
                      log.error(s"Failed to add discipline: ${ex.getMessage}")
                      complete(500, s"Failed to add discipline: ${ex.getMessage}")
                  }

                }
              }
            )
          },
          path(Segment) { disciplineId =>
            val idAsInt = disciplineId.toInt
            concat(
              get {
                complete(DisciplineRepository.getDisciplineById(idAsInt))
              },
              put {
                entity(as[Discipline]) { updatedDiscipline =>
                  val result = for {
                    rowsAffected <- DisciplineRepository.updateDiscipline(idAsInt, updatedDiscipline)
                    _ <- KafkaProducerUtil.sendMessage("discipline-topic", s"Discipline updated: $updatedDiscipline")
                  } yield rowsAffected

                  onComplete(result) {
                    case Success(rowsAffected) =>
                      if (rowsAffected > 0) {
                        complete(s"Rows affected: $rowsAffected")
                      } else {
                        complete(404, "Discipline not found")
                      }
                    case Failure(ex) =>
                      complete(500, s"Update failed: ${ex.getMessage}")
                  }
                }
              },
              delete {
                val result = for {
                  rowsAffected <- DisciplineRepository.deleteDiscipline(idAsInt)
                  _ <- KafkaProducerUtil.sendMessage("discipline-topic", s"Discipline deleted with id: $idAsInt")
                } yield rowsAffected

                onComplete(result) {
                  case Success(rowsAffected) =>
                    if (rowsAffected > 0) {
                      complete(s"Rows affected: $rowsAffected")
                    } else {
                      complete(404, "Discipline not found")
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
