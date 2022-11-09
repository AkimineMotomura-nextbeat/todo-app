package controllers

import lib.model.Category

import ixias.model._

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._
import play.api.data.validation.Constraints._

case class CategoryFormData(
  name: String,
  slug: String,
  color: Category.ColorStatus
)

object CategoryFormData {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "slug" -> nonEmptyText.verifying(Constraints.pattern("[0-9a-zA-Z]+".r)), 
      "color" -> mapping(
        "color" -> number.verifying(min(0), max(255))
      )(x => Category.ColorStatus.apply(x.toShort))(x => Some(x.code.toInt))
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )

  def apply(category: Category.EmbeddedId): Form[CategoryFormData] = 
    form.fill(CategoryFormData(category.v.name, category.v.slug, category.v.color))
}