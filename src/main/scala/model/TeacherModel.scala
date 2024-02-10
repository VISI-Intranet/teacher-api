package model;
case class Teacher(
                    teacherId: Option[Int],
                    education: Option[String],
                    qualification: Option[String],
                    experience: Option[Int],
                    scheduleId: Option[Int],
                    salary: Option[Int],
                    position: Option[String],
                    awards: Option[String],
                    certificationId: Option[Int],
                    attestationId: Option[Int]
                  )