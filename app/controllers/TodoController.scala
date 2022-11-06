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

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import slick.jdbc.JdbcProfile

import model.ViewValueHome
import lib.model.Todo
import lib.model.Category
import lib.persistence._

case class TodoFormData(
  title: String,
  content: String,
  category: Category.Id, 
  state: Todo.Status
)

class TodoController (val controllerComponents: ControllerComponents, val categoryRepos: CategoryRepository[_ <: JdbcProfile], val todoRepos: TodoRepository[_ <: JdbcProfile])
    extends BaseController with play.api.i18n.I18nSupport {

  /**
    * GET /todo/list
    */
  def todoList() = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "Todo list",
      cssSrc = Seq("main.css", "list.css"),
      jsSrc  = Seq("main.js")
    )
    
    val t = todoRepos.all
    for {
      categorys <- categoryRepos.all
      todos <- t
    } yield Ok(views.html.todo.list(todos.map(_.v), categorys.map(_.v), vv))
  }

  
  val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> text,
      "category" -> mapping(
        "category" -> longNumber
      )(Category.Id.apply)(x => Some(x.toLong)), 
      "state" -> mapping(
        "state" -> number.verifying(min(0), max(2))
      )(x => Todo.Status.apply(x.toShort))(x => Some(x.code.toInt))
    )(TodoFormData.apply)(TodoFormData.unapply)
  )
  
  /**
    * GET /todo/:id
    */
  def edit(id: Long) = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css", "editor.css"),
      jsSrc  = Seq("main.js")
    )

    for {
      todo_opt <- todoRepos.get(Todo.Id(id))
      category_seq <- categoryRepos.all
    }yield {
      todo_opt match {
        case Some(todo) => {
          val filledForm = form.fill(TodoFormData(todo.v.title, todo.v.content, todo.v.category, todo.v.state))
          Ok(views.html.todo.editor(todo.id, filledForm, category_seq.map(_.v), vv))
        }
        case None       => NotFound(views.html.error.page404())
      }
    }
  }

  /**
   * GET /todo/new
  */
  def register() = Action async {implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css", "editor.css"),
      jsSrc  = Seq("main.js")
    )
    for {
      categorys <- categoryRepos.all
    } yield {
      val initForm = form.fill(TodoFormData("", "", Category.Id(0), Todo.Status.UNTOUCHED))
      Ok(views.html.todo.register(initForm, categorys.map(_.v), vv))
    }
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

        for {
          category_seq <- categoryRepos.all
        }yield {
          BadRequest(views.html.todo.editor(id, formWithErrors, category_seq.map(_.v), vv))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBのデータをupdate
        val rtv = for {
          todo_old <- todoRepos.get(Todo.Id(id))
        } yield {
          todo_old match {
            case None => Future.successful(NotFound(views.html.error.page404()))
            case Some(todo) => {
              for {
                response <- todoRepos.update(todo.map(_.copy( title=todoFormData.title, 
                                                              content=todoFormData.content, 
                                                              category=todoFormData.category, 
                                                              state=todoFormData.state)))
              } yield {
                response match {
                  case None     => NotFound(views.html.error.page404())
                  case Some(_)  => Redirect("/todo/list")
                }
              }
            }
          }
        }

        rtv.flatten
      }
    )
  }

  /**
   * POST /todo/new
  */
  def store() = Action async {implicit req =>
    form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[TodoFormData]) => {
        val vv = ViewValueHome(
          title  = "",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js")
        )

        for {
          categorys <- categoryRepos.all
        } yield {
          BadRequest(views.html.todo.register(formWithErrors, categorys.map(_.v), vv))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBに追加
        val todo_new = Todo.apply(todoFormData.title, todoFormData.content, category=todoFormData.category)
        for {
          id <- todoRepos.add(todo_new)
        } yield {
          Redirect("/todo/list")
        }
      }
    )
  }

  /**
   * 対象のデータを削除する
   */
  def delete() = Action async { implicit request: Request[AnyContent] =>
    // requestから直接値を取得する
    request.body.asFormUrlEncoded.get("id").headOption match {
      case Some(id_str)  => {
        import scala.util.control.Exception._
        catching(classOf[NumberFormatException]) opt id_str.toLong match {
          case Some(id) => {
            for {
              old <- todoRepos.remove(Todo.Id(id))
            } yield {
              Redirect(routes.TodoController.todoList())
            }
          }
          case None     => Future.successful(BadRequest("/todo/list"))
        }
      }
      case None      => Future.successful(BadRequest("/todo/list"))
    }
  }
}
