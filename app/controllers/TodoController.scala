/**
 *
 * to do sample project
 *
 */

package controllers

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import model.ViewValueHome
import lib.model.Todo
import lib.persistence._

case class TodoFormData(
  title: String,
  content: String,
  category: String
)

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents, todoRepos: TodoRepository[slick.jdbc.JdbcProfile]) //TodoRepository <- 違う書き方がありそう
    extends BaseController with play.api.i18n.I18nSupport {

  //val todoRepos = new TodoRepository[lib.persistence.onMySQL.driver] //reposの宣言方法調べておく

  /**
    * GET /todo/list
    */
  def todoList() = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "Todo list",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    todoRepos.all onComplete {
      case Success(todos) => {
        //seqが空の時の処理を書く

        Ok(views.html.todo.list(todos.map(_.v), vv))
      }
      case Failure(_)        => {
        println("ERROR: TodoController.scala TodoController.todoList")
        NotFound(views.html.error.page404()) //404を仮置き
      }
    }

    //??? 上のUnitからrequestが生成できない分岐があるらしい？
    NotFound(views.html.error.page404())
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
  
  def edit(id: Long) = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    todoRepos.get(Todo.Id(id)) onComplete {
      case Success(todo_option)  => {
        todo_option match {
          case Some(todo)   => {
            val filledForm = form.fill(TodoFormData(todo.v.title, todo.v.content, todo.v.category))
            Ok(views.html.todo.editor(todo.id, filledForm, vv))
          }
          case None         => NotFound(views.html.error.page404())
        }
      }
      case Failure(_)     => NotFound(views.html.error.page404())
    }

    //??? 上のUnitからrequestが生成できない分岐があるらしい？
    NotFound(views.html.error.page404())
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
        //BadRequestを送り返すようにする
        Redirect("/todo/list")
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBのデータをupdate
        todoRepos.get(Todo.Id(id)) onComplete {
          case Success(todo_option)  => {
            todo_option match {
              case Some(todo)   => {
                val todo_udpated = todo.map(_.copy(title=todoFormData.title, content=todoFormData.content, category=todoFormData.category))
                todoRepos.update(todo_udpated)  //成否判定が必要?
              }
              case None         => NotFound(views.html.error.page404())
            }
          }
          case Failure(_)     => NotFound(views.html.error.page404())
        }

        Redirect("/todo/list")
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
        //DBに追加
        val todo_new = Todo.build(todoFormData.title, todoFormData.content, todoFormData.category)
        todoRepos.add(todo_new)

        Redirect("/todo/list")
      }
    )
  }

  /**
   * 対象のデータを削除する
   */
  def delete() = Action { implicit request: Request[AnyContent] =>
    // requestから直接値を取得する
    request.body.asFormUrlEncoded.get("id").headOption match {
      case Some(id_str)  => {
        import scala.util.control.Exception._
        catching(classOf[NumberFormatException]) opt id_str.toLong match {
          case Some(id) => todoRepos.remove(Todo.Id(id)) //<- DBの成否判定が必要?
          case None     => BadRequest("/todo/list")
        }

        Redirect(routes.TodoController.todoList())
      }
      case None      => BadRequest("/todo/list")
    }
  }
}
