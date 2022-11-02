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

    sealed abstract class ColorStatus(val code: Short, val name: String) extends EnumStatus
    object ColorStatus extends EnumStatus.Of[ColorStatus] {
        case object GRAY extends ColorStatus(code = 0, name = "gray")
        case object BLUE extends ColorStatus(code = 1, name = "royalblue")
        case object RED extends ColorStatus(code = 2, name = "tomato")
        case object GREEN extends ColorStatus(code = 3, name = "seagreen")
    }


    def apply(name: String, slug: String, color: ColorStatus): WithNoId = {
        new Entity.WithNoId(
            new Category(
                id = None,
                name = name,
                slug = slug,
                color = color
            )
        )
    }
}