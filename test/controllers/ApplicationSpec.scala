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