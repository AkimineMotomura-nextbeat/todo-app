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
import lib.model.Category
import lib.persistence._

case class TodoFormData(
  title: String,
  content: String,
  category: Long, 
  state: Int
)

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents/*, todoRepos: TodoRepository[slick.jdbc.JdbcProfile]*/) //TodoRepository <- constructorが見つからない???
    extends BaseController with play.api.i18n.I18nSupport {

  val todoRepos = new TodoRepository[slick.jdbc.JdbcProfile]()(onMySQL.driver)
  val categoryRepos = new CategoryRepository[slick.jdbc.JdbcProfile]()(onMySQL.driver)

  /**
    * GET /todo/list
    */
  def todoList() = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "Todo list",
      cssSrc = Seq("main.css", "list.css"),
      jsSrc  = Seq("main.js")
    )
    
    for {
      todos <- todoRepos.all
      categorys <- categoryRepos.all
    } yield Ok(views.html.todo.list(todos.map(_.v), categorys.map(_.v), Category.colors, vv))
  }

  /**
    * GET /todo/:id
    */
  val form = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> text,
      "category" -> longNumber,
      "state" -> number.verifying(min(0), max(2))
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
      category_seq <- categoryRepos.all
    }yield {
      todo_opt match {
        case Some(todo) => {
          val filledForm = form.fill(TodoFormData(todo.v.title, todo.v.content, todo.v.category, todo.v.state.code))
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
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for {
      categorys <- categoryRepos.all
    } yield {
      val initForm = form.fill(TodoFormData("", "", 0, 0))
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
        
        val d: Seq[Category] = Seq[Category]() //仮置き
        Future.successful(BadRequest(views.html.todo.editor(id, formWithErrors, d, vv)))
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        //DBのデータをupdate
        //コーナーケースの抜けができているかも?

        //変更前のtodoを入手
        val todo_old = Await.ready(todoRepos.get(Todo.Id(id)), Duration.Inf) 
        todo_old onComplete {
          case Success(_) => println(todoFormData.category) //debug
          case Failure(_) => throw new java.io.IOException("Failed to fetch a data from DB")
        }

        //todoFormDataから変更情報をコピーしてDB更新
        todo_old.value.get.get match {
          case Some(todo) => {
            for {
              response <- todoRepos.update(todo.map(_.copy( title=todoFormData.title, 
                                                            content=todoFormData.content, 
                                                            category=todoFormData.category, 
                                                            state=Todo.Status.apply(todoFormData.state.toShort))))
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
        Await.ready(todoRepos.add(todo_new), Duration.Inf)

        Future.successful(Redirect("/todo/list"))
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
          case Some(id) => Await.ready(todoRepos.remove(Todo.Id(id)), Duration.Inf) //<- DBの成否判定が必要?
          case None     => BadRequest("/todo/list")
        }

        Redirect(routes.TodoController.todoList())
      }
      case None      => BadRequest("/todo/list")
    }
  }
}
