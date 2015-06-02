6 Minute Apps
===============

This is an introduction to building Modern Web Apps with Play Framework, Scala, CoffeeScript, and LESS


Setup
-----

1. [Download Typesafe Activator](http://typesafe.com/platform/getstarted) (or copy it over from a USB)
2. Extract the zip and run the `activator` or `activator.bat` script from a non-interactive shell
3. Your browser should open to the Activator UI: [http://localhost:8888](http://localhost:8888)


Create a Play App
-----------------

1. Create a new Play Framework application using the `Play Scala Seed` template.
2. As of June 2, 2015 there is [a bug in Activator](https://github.com/typesafehub/activator/issues/1034) that prevents the loading of the project.  To workaround this bug, select *Code > project > play-fork-run.sbt* and change the version of the `sbt-fork-run-plugin` to `2.4.0`.  Then save the file.
3. Run the application by selecting *Run* and then the *Run* button.  You can see the running application at: [http://localhost:9000/](http://localhost:9000/)
4. Run the tests by selecting *Test* and then the *Test* button.

You may want to read the tutorial for the app before continuing.

Open in an IDE
--------------

If you want to use an IDE (Eclipse or IntelliJ) see: https://www.playframework.com/documentation/2.4.x/IDE


Update Dependencies
-------------------

In the `build.sbt` file replace the `libraryDependencies` section with:

    libraryDependencies ++= Seq(
      "org.sorm-framework" % "sorm" % "0.3.18",
      "com.h2database" % "h2" % "1.4.187",
      "org.webjars" %% "webjars-play" % "2.4.0-1",
      "org.webjars" % "bootstrap" % "3.3.4",
      specs2 % Test
    )

This adds the SORM, H2, and WebJar dependencies to the application.  Save the file to reload the changes.  Then re-Run the application.


Cleanup Template Files
----------------------

Remove the following files:

- `public/javascripts`
- `public/stylesheets`
- `test/ApplicationSpec.scala`
- `test/IntegrationSpec.scala`

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
class DB extends Instance(
  entities = Set(Entity[Bar]()),
  url = "jdbc:h2:mem:test"
)
```


Test the Model
--------------

Create a new directory in `test` named `models` and create a new file in that directory called `BarSpec.scala` containing:

```
package models

import javax.inject.Inject

import org.specs2.mutable._

class BarSpec @Inject() (db: DB) extends Specification {

  "Bar" should {
    "be creatable" in {
      val bar = db.save(Bar("foo"))
      bar.id must not (beNull)
      bar.name must beEqualTo ("foo")
    }
  }
  
}
```

In the *Test* tab, select *Test* to re-run the tests.  The only test should pass.


Create a Controller
-------------------

Replace the `app/controllers/Application.scala` contents with:

```
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
```


Map the routes
--------------

In the `conf/routes` file, add the following:

    GET         /bars                 controllers.Application.bars
    POST        /bars                 controllers.Application.addBar


Test the Controller
-------------------

Create a directory named `controllers` in the `test` directory and create a new file in the new directory named `ApplicationSpec.scala` containing:

```
package controllers

import javax.inject.Inject

import models.Bar
import org.specs2.mutable._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec @Inject() (appController: Application) extends Specification {
    
  "Application" should {
    
    "add a bar" in  {
      val addBar = appController.addBar()(FakeRequest(POST, "/bars").withBody(Json.parse("""{"name": "foo"}""")))
      status(addBar) must equalTo(OK)
      Json.parse(contentAsString(addBar)).as[Bar].name must beEqualTo ("foo")
    }
    
    "get all bars" in {
      val bars = appController.bars()(FakeRequest(GET, "/bars"))
      status(bars) must equalTo(OK)
      Json.parse(contentAsString(bars)).as[Seq[Bar]].length must beGreaterThan (0)
    }
    
  }
}
```

In *Test*, select *Test* to re-run the tests.  All three tests should pass.


Asset Compiler Setup
--------------------


Add the following lines to the `build.sbt` file making sure there is an empty line before and after each line:

    LessKeys.compress in Assets := true
    
    pipelineStages := Seq(digest)
    
    includeFilter in (Assets, LessKeys.less) := "*.less"


HTML UI
-------

Add a new route for the WebJars in the `conf/routes` file:

    GET         /webjars/org.webjars/*file        controllers.WebJarAssets.at(file)

In the `app/views/index.scala.html` file, replace `@play20.welcome(message)` with:

    <div class="container">
        <ul id="bars"></ul>
        <form id="barForm" method="post" action="/bars">
            <label for="barName">Name</label>
            <input id="barName" required>
            <button>Add Bar</button>
        </form>
    </div>

In the `app/views/main.scala.html` file add Bootstrap and jQuery loading below the `<title>@title</title>` line:

    <link rel='stylesheet' href='@routes.WebJarAssets.at(WebJarAssets.locate("bootstrap.min.css"))'>
    <script type='text/javascript' src='@routes.WebJarAssets.at(WebJarAssets.locate("jquery.min.js"))'></script>

Replace the contents of the `<body>` section with:

    <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
        <div class="container">
            <div class="navbar-header">
                <a class="navbar-brand" href="#">@title</a>
            </div>
        </div>
    </div>
    <div class="container">
        @content
    </div>

Verify that the app now displays the Bootstrap Nav Bar: [http://localhost:9000/](http://localhost:9000/)


LESS Stylesheet
---------------

Create a new file in a new `app/assets/stylesheets` directory named `main.less` containing:

    body {
      padding-top: 50px;
    }

Verify that the app now displays a simple form: [http://localhost:9000/](http://localhost:9000/)


CoffeeScript UI Logic
---------------------

Create a new file in a new `app/assets/javascripts` directory named `hello.coffee` containing:

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

Verify the app now works and that after adding a new `Bar` it is displayed: [http://localhost:9000/](http://localhost:9000/)
