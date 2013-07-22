package controllers

import models.{DB, Bar}
import models.Bar._

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}


object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("hello, world"))
  }
  
  def bars = Action {
    val bars = DB.query[Bar].fetch()
    Ok(Json.toJson(bars))
  }
  
  def addBar = Action(parse.json) { request =>
    val bar = DB.save(request.body.as[Bar])
    Ok(Json.toJson(bar))
  }
  
}