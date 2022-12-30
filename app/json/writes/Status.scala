package json.writes

import play.api.libs.json.{Json, Writes}

import lib.model.Todo

case class JsValueStatus(
  id: Long,
  name: String
)

object JsValueStatus {
  implicit val writes: Writes[JsValueStatus] = Json.writes[JsValueStatus]

  def apply(state: Todo.Status): JsValueStatus = 
    JsValueStatus(
      id = state.code,
      name = state.name
    )
}