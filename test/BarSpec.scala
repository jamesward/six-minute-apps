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