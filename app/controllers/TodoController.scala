/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import play.api.mvc._

import model.ViewValueHome
import model.Todo
import model.TodoStatus

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  val todos: Seq[Todo] = (1L to 10L).map(i => Todo(i.toString, s"test todo${i.toString}", "Test", TodoStatus.untouched, "test"))

  /**
  * GET /todo/list
  */
  def todoList() = Action { implicit req =>
    //DBから全件入手して放り込む.DBが用意できていないのでこれから
    //fetchTodo(todos)

    val vv = ViewValueHome(
      title  = "Todo list",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    Ok(views.html.todo.list(todos, vv))
  }

  /**
    * GET /todo/:id
    */
    def view(id: String) = Action { implicit req =>
      //DBを叩く

      //DBが無いので動作確認用
      val vv = ViewValueHome(
        title  = "",
        cssSrc = Seq("main.css"),
        jsSrc  = Seq("main.js")
      )

      todos.find(_.id.exists(_ == id)) match{
        case Some(todo) => Ok(views.html.todo.viewer(todo, vv))
        case None        => NotFound(views.html.error.page404())
      }
    }
}
