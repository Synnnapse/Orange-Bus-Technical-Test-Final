package models

/**
  * Created by Michael on 17/11/2015.
  */

import org.joda.time.DateTime

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, nonEmptyText, optional }
import play.api.data.validation.Constraints.pattern

import reactivemongo.bson.{
BSONDateTime, BSONDocument, BSONObjectID
}

import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._

// Using JSON documents for storing and retrieving from the mongo database
case class UserDetails(
  name: String,
  email: String
)

object UserDetails {

  import play.api.libs.json._

  implicit object UserDetailsFormat extends Format[UserDetails] {
    def reads(json: JsValue) = JsSuccess (UserDetails(
      (json \ "name").as[String],
      (json \ "email").as[String]
    ))

    def writes(ud: UserDetails): JsValue = JsObject(Seq(
      "name" -> JsString(ud.name),
      "email" -> JsString(ud.email)
    ))
  }

  implicit object UserDetailsWriter extends OWrites[UserDetails] {

    def writes(details: UserDetails): JsObject = Json.obj(

      "name" -> details.name,
      "email" -> details.email)
  }

  // new form
  val userdetailsform = Form(
    mapping(
      "name" -> text,
      "email" -> text
    )(UserDetails.apply)(UserDetails.unapply))
}

