package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueCreateTodo(
  title: String,
  content: String,
  category: Long,
  state: Short
)

object JsValueCreateTodo {
  implicit val reads: Reads[JsValueCreateTodo] = Json.reads[JsValueCreateTodo]
}