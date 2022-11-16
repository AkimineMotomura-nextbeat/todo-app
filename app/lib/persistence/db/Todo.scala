package lib.persistence.db

import java.time.LocalDateTime
import slick.jdbc.JdbcProfile
import ixias.persistence.model.Table

import lib.model.Todo
import lib.model.Category

case class TodoTable[P <: JdbcProfile]()(implicit val driver: P)
    extends Table[Todo, P] {
    import api._

    lazy val dsn = Map(
        "master"  -> DataSourceName("ixias.db.mysql://master/to_do"),
        "slave"   -> DataSourceName("ixias.db.mysql://slave/to_do")
    )

    class Query extends BasicQuery(new Table(_)) {}
    lazy val query = new Query

    class Table(tag: Tag) extends BasicTable(tag, "to_do") {
      import Todo._
      /* @1 */ def id         = column[Id]            ("id",          O.UInt64, O.PrimaryKey, O.AutoInc)
      /* @2 */ def category   = column[Category.Id]   ("category_id", O.UInt64)
      /* @3 */ def title      = column[String]        ("title",       O.Utf8Char255)
      /* @4 */ def content    = column[String]        ("body",        O.Text)
      /* @5 */ def state      = column[Status]        ("state",       O.UInt8)
      /* @6 */ def updatedAt  = column[LocalDateTime] ("updated_at",  O.TsCurrent)
      /* @7 */ def createdAt  = column[LocalDateTime] ("created_at",  O.Ts)

      type TableElementTuple = (
        Option[Id], Category.Id, String, String, Status, LocalDateTime, LocalDateTime
      )

      def * = (id.?, category, title, content, state, updatedAt, createdAt) <> (
        (t: TableElementTuple) => Todo(
          t._1, t._2, t._3, t._4, t._5, t._6, t._7
        ),
        (v: TableElementType) => Todo.unapply(v).map { t => (
          t._1, t._2, t._3, t._4, t._5, LocalDateTime.now(), t._7
        )}
      )
    }
}