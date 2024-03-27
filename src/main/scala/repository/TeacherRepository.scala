package repository

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


object TeacherRepository {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val rabbitMQModel = RabbitMQModel("TeacherPublisher" , "UniverSystem", "univer.teacher-api.createUserPost")

  def getTeacherById(teacherId: String)(implicit system: ActorSystem, mat: Materializer): Future[Option[Teacher]] = {
    val filter = Filters.eq("_id", teacherId)
    val futureTeacher = MongoDBConnection.teacherCollection.find(filter).headOption()

    futureTeacher.map {
      case Some(doc) =>
        Some(
          Teacher(
            _id = doc.getString("_id"),
            name = Option(doc.getString("name")),
            age = Option(doc.getInteger("age")).map(_.toInt),
            email = Option(doc.getString("email")),
            phoneNumber = Option(doc.getString("phoneNumber")),
            education = Option(doc.getString("education")),
            qualification = Option(doc.getString("qualification")),
            experience = Option(doc.getInteger("experience")).map(_.toInt),
            scheduleId = Option(doc.getInteger("scheduleId")).map(_.toInt),
            disciplinesId = Option(Option(doc.getList("disciplinesId", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)),
            studentsId = Option(Option(doc.getList("studentsId", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)),
            salary = Option(doc.getInteger("salary")).map(_.toInt),
            position = Option(doc.getString("position")),
            awards = Option(doc.getString("awards")),
            certificationId = Option(doc.getString("certificationId")),
            attestationId = Option(doc.getString("attestationId")),
            discipline = None,
            students = None,
            documents = None,
            studentsAverage = None
          )
        )
      case None => None
    }
  }

  def getAllTeachers(): Future[List[Teacher]] = {
    val futureTeachers = MongoDBConnection.teacherCollection.find().toFuture()
    import org.mongodb.scala.bson.Document

    futureTeachers.map { docs =>
      Option(docs).map(_.map { doc =>
        Teacher(
          _id = doc.getString("_id"),
          name = Option(doc.getString("name")),
          age = Option(doc.getInteger("age")).map(_.toInt),
          email = Option(doc.getString("email")),
          phoneNumber = Option(doc.getString("phoneNumber")),
          education = Option(doc.getString("education")),
          qualification = Option(doc.getString("qualification")),
          experience = Option(doc.getInteger("experience")).map(_.toInt),
          scheduleId = Option(doc.getInteger("scheduleId")).map(_.toInt),
          disciplinesId = Option(Option(doc.getList("disciplinesId", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)),
          studentsId = Option(Option(doc.getList("studentsId", classOf[String])).map(_.asScala.toList).getOrElse(List.empty)),
          salary = Option(doc.getInteger("salary")).map(_.toInt),
          position = Option(doc.getString("position")),
          awards = Option(doc.getString("awards")),
          certificationId = Option(doc.getString("certificationId")),
          attestationId = Option(doc.getString("attestationId")),
          documents = None,
          discipline = None,
            students = None,
          studentsAverage = None

        )
      }.toList).getOrElse(List.empty)
    }
  }


  def addTeacher(teacher: Teacher)(implicit system: ActorSystem, mat: Materializer): Future[String] = {
    val teacherDocument = BsonDocument(
      "_id" -> BsonString(teacher._id),
      "name" -> teacher.name.map(BsonString.apply).getOrElse(BsonNull()),
      "age" -> teacher.age.map(BsonInt32.apply).getOrElse(BsonNull()),
      "email" -> teacher.email.map(BsonString.apply).getOrElse(BsonNull()),
      "phoneNumber" -> teacher.phoneNumber.map(BsonString.apply).getOrElse(BsonNull()),
      "education" -> teacher.education.map(BsonString.apply).getOrElse(BsonNull()),
      "qualification" -> teacher.qualification.map(BsonString.apply).getOrElse(BsonNull()),
      "experience" -> teacher.experience.map(BsonInt32.apply).getOrElse(BsonNull()),
      "scheduleId" -> teacher.scheduleId.map(BsonInt32.apply).getOrElse(BsonNull()),
      "disciplinesId" -> BsonArray(teacher.disciplinesId.getOrElse(List.empty).map(BsonString.apply)),
      "studentsId" -> BsonArray(teacher.studentsId.getOrElse(List.empty).map(BsonString.apply)),
      "salary" -> teacher.salary.map(BsonInt32.apply).getOrElse(BsonNull()),
      "position" -> teacher.position.map(BsonString.apply).getOrElse(BsonNull()),
      "awards" -> teacher.awards.map(BsonString.apply).getOrElse(BsonNull()),
      "certificationId" -> teacher.certificationId.map(BsonString.apply).getOrElse(BsonNull()),
      "attestationId" -> teacher.attestationId.map(BsonString.apply).getOrElse(BsonNull()),
    )



    MongoDBConnection.teacherCollection.insertOne(teacherDocument).toFuture().map(_ => s"Учитель с teacherId - ${teacher._id} был добавлен в базу данных ;)")
  }

  def deleteTeacher(teacherId: String): Future[String] = {
    val teacherDocument = Document("_id" -> teacherId)
    MongoDBConnection.teacherCollection.deleteOne(teacherDocument).toFuture().map(_ => s"Учитель с teacherId $teacherId был удален, проверьте БД ;)")
  }

  def updateTeacher(teacherId: String, updatedTeacher: Teacher): Future[String] = {
    val filter = Document("_id" -> teacherId)

    val teacherDocument = BsonDocument(
      "$set" -> BsonDocument(
        "name" -> updatedTeacher.name.map(BsonString.apply).getOrElse(BsonNull()),
        "age" -> updatedTeacher.age.map(BsonInt32.apply).getOrElse(BsonNull()),
        "email" -> updatedTeacher.email.map(BsonString.apply).getOrElse(BsonNull()),
        "phoneNumber" -> updatedTeacher.phoneNumber.map(BsonString.apply).getOrElse(BsonNull()),
        "education" -> BsonString(updatedTeacher.education.getOrElse("")),
        "qualification" -> BsonString(updatedTeacher.qualification.getOrElse("")),
        "experience" -> BsonInt32(updatedTeacher.experience.getOrElse(0)),
        "scheduleId" -> BsonInt32(updatedTeacher.scheduleId.getOrElse(0)),
        "disciplinesId" -> BsonArray(updatedTeacher.disciplinesId.getOrElse(List.empty).map(BsonString(_))),
        "studentsId" -> BsonArray(updatedTeacher.studentsId.getOrElse(List.empty).map(BsonString.apply)),
        "salary" -> BsonInt32(updatedTeacher.salary.getOrElse(0)),
        "position" -> BsonString(updatedTeacher.position.getOrElse("")),
        "awards" -> BsonString(updatedTeacher.awards.getOrElse("")),
        "certificationId" -> BsonString(updatedTeacher.certificationId.getOrElse("")),
        "attestationId" -> BsonString(updatedTeacher.attestationId.getOrElse("")),
      )
    )

    MongoDBConnection.teacherCollection.updateOne(filter, teacherDocument).toFuture().map { updatedResult =>
      if (updatedResult.wasAcknowledged() && updatedResult.getModifiedCount > 0) {
        s"Учитель был обновлен, teacherId: $teacherId - не знаю что изменилось, проверяй в БД ;)"
      } else {
        "Обновление учителя не выполнено: Проблема либо в базе, либо в тебе ;)"
      }
    }
  }


}
