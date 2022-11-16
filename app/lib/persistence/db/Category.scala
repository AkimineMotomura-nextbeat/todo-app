package lib.persistence.db

import java.time.LocalDateTime
import slick.jdbc.JdbcProfile
import ixias.persistence.model.Table

import lib.model.Category

case class CategoryTable[P <: JdbcProfile]()(implicit val driver: P)
    extends Table[Category, P] {
    import api._

    lazy val dsn = Map(
        "master"  -> DataSourceName("ixias.db.mysql://master/to_do"),
        "slave"   -> DataSourceName("ixias.db.mysql://slave/to_do")
    )

    class Query extends BasicQuery(new Table(_)) {}
    lazy val query = new Query

    class Table(tag: Tag) extends BasicTable(tag, "to_do_category") {
        import Category._
        /* @1 */ def id         = column[Id]            ("id",          O.UInt64, O.PrimaryKey, O.AutoInc)
        /* @2 */ def name       = column[String]        ("name",        O.Utf8Char255)
        /* @3 */ def slug       = column[String]        ("slug",        O.Utf8Char255)
        /* @4 */ def color      = column[ColorStatus]   ("color",       O.UInt8)
        /* @5 */ def updatedAt  = column[LocalDateTime] ("updated_at",  O.TsCurrent)
        /* @6 */ def createdAt  = column[LocalDateTime] ("created_at",  O.Ts)

        type TableElementTuple = (
            Option[Id], String, String, ColorStatus, LocalDateTime, LocalDateTime
        )

        def * = (id.?, name, slug, color, updatedAt, createdAt) <> (
            (t: TableElementTuple) => Category(
                t._1, t._2, t._3, t._4, t._5, t._6
            ),
            (v: TableElementType) => Category.unapply(v).map { t => (
                t._1, t._2, t._3, t._4, LocalDateTime.now(), t._6
            )}
        )
    }
}