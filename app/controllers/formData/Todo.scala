package controllers

import lib.model.Todo
import lib.model.Category

import ixias.model._

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import play.api.data.validation.Constraints._

case class TodoFormData(
  title: String,
  content: String,
  category: Category.Id, 
  state: Todo.Status
)

object TodoFormData {
    val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> text,
      "category" -> mapping(
        "category" -> longNumber
      )(Category.Id.apply)(x => Some(x.toLong)), 
      "state" -> mapping(
        "state" -> number.verifying(min(0), max(2))
      )(x => Todo.Status.apply(x.toShort))(x => Some(x.code.toInt))
    )(TodoFormData.apply)(TodoFormData.unapply)
  )

  def apply(todo: Todo.EmbeddedId): Form[TodoFormData] = 
    form.fill(TodoFormData(todo.v.title, todo.v.content, todo.v.category, todo.v.state))

  def init_form: Form[TodoFormData] = 
    form.fill(TodoFormData("", "", Category.Id(6), Todo.Status.UNTOUCHED))
}