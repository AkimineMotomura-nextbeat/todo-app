package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

import Todo._
case class Todo(
    id:         Option[Id],
    category:   Category.Id,
    title:      String,
    content:    String,
    state:      Status,
    updatedAt:  LocalDateTime = NOW,
    createdAt:  LocalDateTime = NOW
) extends EntityModel[Id]

//コンパニオンオブジェクト
object Todo {
    val Id = the[Identity[Id]]
    type Id = Long @@ Todo
    type WithNoId = Entity.WithNoId[Id, Todo]
    type EmbeddedId = Entity.EmbeddedId[Id, Todo]

    //ステータス
    sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
    object Status extends EnumStatus.Of[Status] {
        case object UNTOUCHED extends Status(code = 0, name = "TODO(着手前)")
        case object TOUCHED extends Status(code = 1, name = "進行中")
        case object COMPLETE extends Status(code = 2, name = "完了")
    }

    def apply(title: String, content: String, category: Category.Id): WithNoId = {
        new Entity.WithNoId(
            new Todo(
                id = None,
                title = title,
                content = content,
                state = Status.UNTOUCHED,
                category = category
            )
        )
    }
}