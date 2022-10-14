package slick.model

import java.time.LocalDateTime

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
  category: String,
  postedAt:  LocalDateTime = LocalDateTime.now,
  createdAt: LocalDateTime = LocalDateTime.now,
  updatedAt: LocalDateTime = LocalDateTime.now
) 