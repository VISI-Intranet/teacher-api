package repository.AdditionalRepo

import Alpakka.RabbitMQModel.RabbitMQModel
import akka.actor.ActorSystem
import akka.stream.Materializer
import model._
import connection.MongoDBConnection
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonInt32, BsonNull, BsonString}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.Filters.in

object Repo {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def getTeachersNamesByIds(teacherIds: List[String])(implicit system: ActorSystem, mat: Materializer): Future[List[Option[String]]] = {
    val filter = in("_id", teacherIds: _*)
    val futureTeachers = MongoDBConnection.teacherCollection.find(filter).toFuture()

    futureTeachers.map { documents =>
      documents.map { doc =>
        Option(doc.getString("name"))
      }.toList
    }
  }


}
