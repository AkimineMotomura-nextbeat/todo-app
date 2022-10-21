/**
 *
 * to do sample project
 *
 */

package controllers

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.duration._

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
  category: Long
)

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents/*, todoRepos: TodoRepository[slick.jdbc.JdbcProfile]*/) //TodoRepository <- constructorが見つからない???
    extends BaseController with play.api.i18n.I18nSupport {

  val todoRepos = new TodoRepository[slick.jdbc.JdbcProfile]()(onMySQL.driver)

  /**
    * GET /todo/list
    */
  def todoList() = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "Todo list",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    
    for {
      todos <- todoRepos.all
    } yield Ok(views.html.todo.list(todos.map(_.v), vv))
    
  }

  /**
    * GET /todo/:id
    */
  val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> text,
      "category" -> longNumber
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
  
  def edit(id: Long) = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    for {
      todo_opt <- todoRepos.get(Todo.Id(id))
    }yield {
      todo_opt match {
        case Some(todo) => {
          val filledForm = form.fill(TodoFormData(todo.v.title, todo.v.content, todo.v.category)) //なぜかcategoryがインクリメントされている???
          Ok(views.html.todo.editor(todo.id, filledForm, vv))
        }
        case None       => NotFound(views.html.error.page404())
      }
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
  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[TodoFormData]) => {
        val vv = ViewValueHome(
          title  = "",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js")
        )
        
        Future.successful(BadRequest(views.html.todo.editor(id, formWithErrors, vv))) //BadRequestを送り返すようにする
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBのデータをupdate
        //コーナーケースの抜けができている
        val todo_old = Await.ready(todoRepos.get(Todo.Id(id)), Duration.Inf) 
        todo_old onComplete {
          case Failure(_) => {
            NotFound(views.html.error.page404())
          }
        }

        todo_old.value.get.get match {
          case Some(todo) => {
            for {
              response <- todoRepos.update(todo.map(_.copy(title=todoFormData.title, content=todoFormData.content, category=todoFormData.category)))
            } yield {
              response match {
                case None     => NotFound(views.html.error.page404())
                case Some(_)  => Redirect("/todo/list")
              }
            }
          }
          case None       => {
            Future.successful(NotFound(views.html.error.page404()))
          }
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
        BadRequest(views.html.todo.register(formWithErrors, vv))
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBに追加
        val todo_new = Todo.apply(todoFormData.title, todoFormData.content, todoFormData.category)
        Await.ready(todoRepos.add(todo_new), Duration.Inf)

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
