package json.writes

import play.api.libs.json.{Json, Writes}

import lib.model.Category

case class JsValueColor(
  id: Long,
  name: String, 
  colorCode: String
)

object JsValueColor {
  implicit val writes: Writes[JsValueColor] = Json.writes[JsValueColor]

  def apply(color: Category.ColorStatus): JsValueColor = 
    JsValueColor(
      id = color.code,
      name = color.name,
      colorCode = color.colorCode
    )
}