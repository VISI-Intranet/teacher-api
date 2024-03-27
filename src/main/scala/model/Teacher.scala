package model

import io.circe.Json
import model.importModel.{ImportDiscipline, ImportDocumentation, ImportStudents}

case class Teacher(
                    _id: String,
                    name: Option[String],
                    age: Option[Int],
                    email: Option[String],
                    phoneNumber: Option[String],
                    education: Option[String],
                    qualification: Option[String],
                    experience: Option[Int],
                    scheduleId: Option[Int],
                    disciplinesId: Option[List[String]],
                    studentsId: Option[List[String]],
                    salary: Option[Int],
                    position: Option[String],
                    awards: Option[String],
                    certificationId: Option[String],
                    attestationId: Option[String],
                    discipline: Option[List[ImportDiscipline]],
                    students: Option[List[ImportStudents]],
                    studentsAverage: Option[Map[String,Int]],
                    documents: Option[List[ImportDocumentation]]

                  )
