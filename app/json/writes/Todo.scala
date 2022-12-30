package json.writes

import play.api.libs.json.{Json, Writes}

import lib.model.Todo

case class JsValueTodo(
  id: Long,
  category: Long,
  title: String, 
  content: String,
  state: Short
)

object JsValueTodo {
  implicit val writes: Writes[JsValueTodo] = Json.writes[JsValueTodo]

  def apply(todo: Todo.EmbeddedId): JsValueTodo = 
    JsValueTodo(
      id = todo.id,
      category = todo.v.category,
      title = todo.v.title,
      content = todo.v.content,
      state = todo.v.state.code
    )
}