package controllers

/**
  * Created by Michael on 17/11/2015.
  */

import javax.inject.Inject

import play.api.i18n.MessagesApi
import play.api.libs.json
import reactivemongo.api.Cursor

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{ Action, Controller }
import play.api.libs.json._

import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import play.modules.reactivemongo.{
  MongoController, ReactiveMongoApi, ReactiveMongoComponents
}

import play.modules.reactivemongo.json.collection.{
JSONCollection
}

import play.modules.reactivemongo.json.collection._
import models.UserDetails, UserDetails._

import java.util.UUID

import play.api.libs.json._

class UserDetails @Inject() (val messagesApi: MessagesApi, val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  // Get our simple collection from the mongo database specified in config
  def userDetailsCollection : JSONCollection = db.collection("userdetails")

  // Every time we go to the home page, find all users and just display them
  def index = Action.async { request =>

    val cursor: Cursor[JsObject] = userDetailsCollection.find(Json.obj()).cursor[JsObject]

    val futureUsersList: Future[List[JsObject]] = cursor.collect[List]()

    futureUsersList.map { persons => Ok(views.html.index(persons, "Heyeyeyeye")) }.recover
    {
      case e => e.printStackTrace()
        BadRequest(e.getMessage())
    }
  }

  // Search for a user from a form
  def searchForUser = Action.async { implicit request =>
      implicit val messages = messagesApi.preferred(request)

      UserDetails.userdetailsform.bindFromRequest.fold(
        formWithErrors => { Future.successful(BadRequest(views.html.searchEntry(formWithErrors)))},
        searchData => {

          val nameToSearch: String = searchData.name

          val cursor: Cursor[JsObject] = userDetailsCollection.find(Json.obj("name" -> nameToSearch)).cursor[JsObject]
          val futurePersonsList: Future[List[JsObject]] = cursor.collect[List]()
          futurePersonsList.map { persons => Ok(views.html.searchResults(nameToSearch, persons)) }
        })
  }

  // Just show the add user form
  def displayAddUserForm = Action { request =>
    implicit val messages = messagesApi.preferred(request)

    Ok(views.html.addUser(UserDetails.userdetailsform))
  }

  // Just show the search for a user form
  def displaySearchForUserForm = Action { request =>
    implicit val messages = messagesApi.preferred(request)

    Ok(views.html.searchEntry(UserDetails.userdetailsform))
  }

  // Add a new user POST request using reactivemongo and Play's Future requests
  def addNewUser() = Action.async { implicit request =>
    implicit val messages = messagesApi.preferred(request)

    UserDetails.userdetailsform.bindFromRequest.fold(
      formWithErrors => { Future.successful(BadRequest(views.html.addUser(formWithErrors)))},
      userDetailsData => {

        val userToInsert = models.UserDetails(userDetailsData.name, userDetailsData.email)

        userDetailsCollection.insert(userToInsert).map(_ => Redirect(routes.UserDetails.index).flashing("Success" -> "User Saved!")) } )}
}
