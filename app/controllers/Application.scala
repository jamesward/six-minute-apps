package controllers

import javax.inject.Inject

import models.{DB, Bar}
import models.Bar._

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}


class Application @Inject() (db: DB) extends Controller {
  
  def index = Action {
    Ok(views.html.index("hello, world"))
  }
  
  def bars = Action {
    val bars = db.query[Bar].fetch()
    Ok(Json.toJson(bars))
  }
  
  def addBar = Action(parse.json) { request =>
    val bar = db.save(request.body.as[Bar])
    Ok(Json.toJson(bar))
  }
  
}