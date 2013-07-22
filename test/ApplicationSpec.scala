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