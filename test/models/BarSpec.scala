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