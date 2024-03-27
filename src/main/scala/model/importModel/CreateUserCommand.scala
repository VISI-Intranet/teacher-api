package model.importModel

case class CreateUserCommand(
                              _id: String,
                              name: Option[String],
                              age: Option[Int],
                              email: Option[String],
                              phoneNumber: Option[String],
                              checkCreate: Boolean = false,
                              userType: String = "Teacher"
                            )
