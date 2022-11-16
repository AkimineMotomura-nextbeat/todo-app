/**
 *
 * to do sample project
 *
 */

package model

// Topページのviewvalue
case class ViewValueHome(
  title:  String,
  cssSrc: Seq[String],
  jsSrc:  Seq[String],
) extends ViewValueCommon

object ViewValueHome {
  val vv_list = ViewValueHome(
      title  = "Category list",
      cssSrc = Seq("main.css", "list.css"),
      jsSrc  = Seq("main.js")
    )

  val vv_edit = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css", "editor.css"),
      jsSrc  = Seq("main.js")
  )
}