package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

import Todo._
case class Todo(
    id:         Option[Id],
    title:      String,
    content:    String,
    state:      Status,
    category:   String,
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
        case object UNTOUCHED extends Status(code = 0, name = "untouched")
        case object TOUCHED extends Status(code = 1, name = "touched")
        case object COMPLETE extends Status(code = 100, name = "complete")
    }

    def apply(title: String, content: String, state: Status, category: String): WithNoId = {
        new Entity.WithNoId(
            new Todo(
                id = None,
                title = title,
                content = content,
                state = state,
                category = category
            )
        )
    }

    def build(title: String): Todo#WithNoId =
        new Todo(
            id = None,
            title = title,
            content = "",
            state = Status.UNTOUCHED, 
            category = ""
        ).toWithNoId
}