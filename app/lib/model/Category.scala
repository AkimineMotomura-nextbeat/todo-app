package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

import Category._
case class Category(
    id:         Option[Id],
    name:       String,
    slug:       String,
    color:      ColorStatus,
    updatedAt:  LocalDateTime = NOW,
    createdAt:  LocalDateTime = NOW
) extends EntityModel[Id]

object Category {
    val Id = the[Identity[Id]]
    type Id = Long @@ Category
    type WithNoId = Entity.WithNoId[Id, Category]
    type EmbeddedId = Entity.EmbeddedId[Id, Category]

    val noCategory_id = Category.Id(6)

    sealed abstract class ColorStatus(val code: Short, val name: String, val colorCode: String) extends EnumStatus
    object ColorStatus extends EnumStatus.Of[ColorStatus] {
        case object GRAY extends ColorStatus(code = 0, name = "GRAY", colorCode = "gray")
        case object BLUE extends ColorStatus(code = 1, name = "BLUE", colorCode = "royalblue")
        case object RED extends ColorStatus(code = 2, name = "RED", colorCode = "tomato")
        case object GREEN extends ColorStatus(code = 3, name = "GREEN", colorCode = "seagreen")
    }


    def apply(name: String, slug: String, color: ColorStatus): WithNoId = {
        Entity.WithNoId.apply(
            new Category(
                id = None,
                name = name,
                slug = slug,
                color = color
            )
        )
    }
}