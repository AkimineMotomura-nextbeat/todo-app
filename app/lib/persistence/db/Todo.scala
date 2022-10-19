package lib.persistence.db

import java.time.LocalDateTime
import slick.jdbc.JdbcProfile
import ixias.persistence.model.Table

import lib.model.Todo

case class TodoTable[P <: JdbcProfile]()(implicit val driver: P)
    extends Table[Todo, P] {
    import api._

    lazy val dsn = Map(
        "master"  -> DataSourceName("ixias.db.mysql://master/todo"),
        "slave"   -> DataSourceName("ixias.db.mysql://slave/todo")
    )

    class Query extends BasicQuery(new Table(_)) {}
    lazy val query = new Query

    class Table(tag: Tag) extends BasicTable(tag, "todo") {
      import Todo._
      /* @1 */ def id         = column[Id]            ("id",          O.UInt64, O.PrimaryKey, O.AutoInc)
      /* @2 */ def title      = column[String]        ("title",       O.Utf8Char255)
      /* @3 */ def content    = column[String]        ("content",     O.Utf8Char255)
      /* @4 */ def state      = column[Status]        ("state",       O.UInt8)
      /* @5 */ def category   = column[String]        ("category",    O.Utf8Char255)
      /* @6 */ def updatedAt  = column[LocalDateTime] ("updated_at",  O.TsCurrent)
      /* @7 */ def createdAt  = column[LocalDateTime] ("created_at",  O.Ts)

      type TableElementTuple = (
        Option[Id], String, String, Status, String, LocalDateTime, LocalDateTime
      )

      def * = (id.?, title, content, state, category, updatedAt, createdAt) <> (
        (t: TableElementTuple) => Todo(
          t._1, t._2, t._3, t._4, t._5, t._6, t._7
        ),
        (v: TableElementType) => Todo.unapply(v).map { t => (
          t._1, t._2, t._3, t._4, t._5, LocalDateTime.now(), t._7
        )}
      )
    }
}