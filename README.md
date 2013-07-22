# 6 Minute Apps
===============

This is an introduction to building Modern Web Apps with Play Framework, Scala, CoffeeScript, and LESS


Setup
-----

1. [Download Typesafe Activator](http://typesafe.com/platform/getstarted)
2. Extract the zip and run the `activator` script from a non-interactive shell
3. Your browser should open to the Activator UI: [http://localhost:8888](http://localhost:8888)


Create a Play App
-----------------

Create a new Play Framework application using the `Hello Play Framework!` template.  This will compile the application then run it and it's tests.  You can see the running application at: [http://localhost:9000/](http://localhost:9000/)


Open in an IDE
--------------

If you want to use an IDE (Eclipse or IntelliJ), click on *Code*, select *Open*, and then select your IDE.  This will walk you through the steps to generate the project files and open the project.  Alternatively you can edit files in the Activator UI.


Update Dependencies
-------------------

In the `project/Build.scala` file add the following lines to the `appDependencies` section after the line containing `javaCore`:

    "org.sorm-framework" % "sorm" % "0.3.8",
    "com.h2database" % "h2" % "1.3.168",

This adds the SORM and H2 dependencies to the application.  Refresh the page in order to reload the build definition and dependencies.


Cleanup Template Files
----------------------

Remove the following files:

- `app/assets/javascripts/index.js`
- `app/controllers/MainController.java`
- `app/controllers/MessageController.scala`
- `test/IntegrationTest.java`
- `test/MainControllerTest.java`
- `test/MessageControllerSpec.scala`

Delete the following lines from the `conf/routes` file:

    GET     /                           controllers.MainController.index()
    GET     /message                    controllers.MessageController.getMessage()
    GET     /assets/javascripts/routes  controllers.MessageController.javascriptRoutes()

Remove the following line from the `app/views/main.scala.html` file:

    <script type="text/javascript" src="@routes.MessageController.javascriptRoutes"></script>

Verify that the app compiles without any errors.


Create a Model
--------------

Create a new directory under `app` named `models`.  Create a new file named `Bar.scala` in the `app/models` directory containing:

```
package models

import play.api.libs.json.{JsValue, Writes, Json}
import sorm.Persisted

case class Bar(name: String)

object Bar {
  implicit val barWrites = new Writes[Bar with Persisted] {
    def writes(bar: Bar with Persisted): JsValue = {
      Json.obj(
        "id" -> bar.id,
        "name" -> bar.name
      )
    }
  }
  implicit val barReads = Json.reads[Bar]
}

import sorm._
object DB extends Instance(
  entities = Set(Entity[Bar]()),
  url = "jdbc:h2:mem:test"
)
```


Test the Model
--------------

Create a new file in the `test` directory named `BarSpec.scala` containing:

```
import models.{DB, Bar}

import org.specs2.mutable.Specification
import play.api.test._

class BarSpec extends Specification {

  "Bar" should {
    "be creatable" in new WithApplication {
      val bar = DB.save(Bar("foo"))
      bar.id must not (beNull)
      bar.name must beEqualTo ("foo")
    }
  }
  
}
```

In the *Test* tab, select *Start* to re-run the tests.  The only test should pass.


Create a Controller
-------------------

In the `app/controllers` directory create a new file named `Application.scala` containing:

```
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
```


Map the routes
--------------

In the `conf/routes` file, add the following at line 5:

    GET     /                           controllers.Application.index
    GET     /bars                       controllers.Application.bars
    POST    /bars                       controllers.Application.addBar


Test the Controller
-------------------

Create a new file in the `test` directory named `ApplicationSpec.scala` containing:

```
package test

import controllers.Application
import models.Bar

import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
    
  "Application" should {
    
    "add a bar" in new WithApplication {
      val addBar = Application.addBar()(FakeRequest(POST, "/bars").withBody(Json.parse("""{"name": "foo"}""")))
      status(addBar) must equalTo(OK)
      Json.parse(contentAsString(addBar)).as[Bar].name must beEqualTo ("foo")
    }
    
    "get all bars" in new WithApplication {
      val bars = Application.bars()(FakeRequest(GET, "/bars"))
      status(bars) must equalTo(OK)
      Json.parse(contentAsString(bars)).as[Seq[Bar]].length must beGreaterThan (0)
    }
    
  }
}
```

In the *Test* tab, select *Start* to re-run the tests.  All three tests should pass.


HTML UI
-------

In the `app/views/index.scala.html` file, replace the contents beneath the `script` tag with:

        <div class="container">
            <ul id="bars"></ul>
            <form id="barForm" method="post" action="/bars">
                <label for="barName">Name</label>
                <input id="barName" required>
                <button>Add Bar</button>
            </form>
        </div>

Verify that the app now contains the form: [http://localhost:9000/](http://localhost:9000/)


CoffeeScript UI Logic
---------------------

Create a new file in the `app/assets/javascripts/` directory named `index.coffee` containing:

```
getBars = () ->
  $.get "/bars", (bars) ->
    $("#bars").empty()
    $.each bars, (index, bar) ->
      $("#bars").append $("<li>").text bar.name

$ ->
  getBars()
  $("#barForm").submit (event) ->
    event.preventDefault()
    $.ajax
      url: event.target.action
      type: event.target.method
      contentType: "application/json"
      data: JSON.stringify({name: $("#barName").val()})
      success: () ->
        getBars()
        $("#barName").val("")
```

Verify the app now works: [http://localhost:9000/](http://localhost:9000/)
