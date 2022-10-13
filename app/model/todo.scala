package model

sealed trait TodoStatus

object TodoStatus{
  case object untouched extends TodoStatus
  case object touched extends TodoStatus
  case object complete extends TodoStatus
}

/**
 * todoのモデル
 */
case class Todo(
  id: String,
  title:  String,
  content: String,
  status: TodoStatus,
  category: String
) 