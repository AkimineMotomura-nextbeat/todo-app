/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import model.ViewValueHome
//import lib.model.Todo
import lib.persistence._

import slick.model.Todo
import slick.model.TodoStatus

case class TodoFormData(
  title: String,
  content: String,
  category: String
)

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with play.api.i18n.I18nSupport {

  var todos: Seq[Todo] = (1L to 10L).map(i => Todo(i, s"test todo${i.toString}", "Test", TodoStatus.untouched, "test"))
  //val todoRepos: TodoRepository[Todo]

  /**
    * GET /todo/list
    */
  def todoList() = Action { implicit req =>
    //DBから全件入手して放り込む.DBが用意できていないのでこれから
    //todos = fetchTodo()

    //val todos: Seq[Todo]

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
  val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> text,
      "category" -> text
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
/*
  def edit(id: Long) = Action.async { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    todoRepos.get(Todo.Id(id)).value.get.get match{
      case Some(todo)  => {
        val filledForm = form.fill(TodoFormData(todo, todo.content, todo.category))
        Ok(views.html.todo.editor(todo, filledForm, vv))
      }
      case None        => {
        //
      }
    }
  }
*/  
  
  def edit(id: Long) = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    //DBを叩く
    //fetch(id)
    
    //DBが無いので動作確認用
    todos.find(_.id == id) match{
      case Some(todo) => {
        val filledForm = form.fill(TodoFormData(todo.title, todo.content, todo.category))
        Ok(views.html.todo.editor(todo.id, filledForm, vv))
      }
      case None        => NotFound(views.html.error.page404())
    }
  }

  /**
   * GET /todo/new
  */
  def register() = Action {implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.todo.register(form, vv))
  }

  /**
    * POST /todo/:id/update
    */
  def update(id: Long) = Action { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[TodoFormData]) => {
        val vv = ViewValueHome(
          title  = "",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js")
        )
        
        todos.find(_.id == id) match{
          case Some(todo) => BadRequest(views.html.todo.editor(todo.id, form, vv)) //form <- formWithErrorに修正
          case None        => NotFound(views.html.error.page404())
        }
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        val todo = Todo(id, todoFormData.title, todoFormData.content, TodoStatus.untouched, todoFormData.category)
          todos.find(_.id == id) match{
            case Some(todo_pre) => {
              todos = todos.updated(todos.indexOf(todo_pre), todo)

              Redirect("/todo/list")
            }
            case None        => NotFound(views.html.error.page404())
          }
      }
    )
  }

  /**
   * POST /todo/new
  */
  def store() = Action {implicit req =>
    form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[TodoFormData]) => {
        val vv = ViewValueHome(
          title  = "",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js")
        )
        BadRequest(views.html.todo.register(form, vv)) //form <- formWithErrorに修正
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //id生成. 下記は仮
        val todo_id = (todos.length + 1)
        val todo = Todo(todo_id, todoFormData.title, todoFormData.content, TodoStatus.untouched, todoFormData.category)
        todos = todos :+ todo

        Redirect("/todo/list")
      }
    )
  }

  /**
   * 対象のデータを削除する
   */
  def delete() = Action { implicit request: Request[AnyContent] =>
    // requestから直接値を取得する
    val idOpt = request.body.asFormUrlEncoded.get("id").headOption
    // idがあり、値もあるときに削除
    todos.find(_.id.toString == idOpt.get) match {
      case Some(todo) =>
        todos = todos.filterNot(_.id.toString == idOpt.get)
        // 削除が完了したら一覧ページへリダイレクト
        Redirect(routes.TodoController.todoList())
      case None        =>
        NotFound(views.html.error.page404())
    }
  }
}
