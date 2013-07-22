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