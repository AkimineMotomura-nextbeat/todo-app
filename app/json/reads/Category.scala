package json.reads

import play.api.libs.json.{Json, Reads}
import play.api.libs.json.JsPath
import play.api.libs.functional.syntax._

import lib.model.Category

case class JsValueCreateCategory(
  id: Long,
  name: String,
  slug: String,
  color: Short
)

object JsValueCreateCategory {
  implicit val reads: Reads[JsValueCreateCategory] = Json.reads[JsValueCreateCategory]
}