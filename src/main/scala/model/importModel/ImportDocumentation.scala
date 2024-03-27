package model.importModel

case class ImportDocumentation(_id:Option[String] = None,
                         docid:Int,
                         title: String,
                         userid: Int,
                         courseid: Int,
                         dateuploaded:String,
                         documenttype:String,
                         tags:String,
                         fileURL:String)