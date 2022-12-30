package json.writes

import play.api.libs.json.{Json, Writes}

import lib.model.Category

case class JsValueCategory(
  id: Long,
  name: String, 
  slug: String,
  color: Short
)

object JsValueCategory {
  implicit val writes: Writes[JsValueCategory] = Json.writes[JsValueCategory]

  def apply(category: Category.EmbeddedId): JsValueCategory = 
    JsValueCategory(
      id = category.id,
      name = category.v.name,
      slug = category.v.slug,
      color = category.v.color.code
    )
}