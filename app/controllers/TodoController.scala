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
import slick.model.Todo
import slick.model.TodoStatus

case class TodoFormData(
  title: String,
  content: String,
  category: String
)

@Singleton
class TodoController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with play.api.i18n.I18nSupport {

  var todos: Seq[Todo] = (1L to 10L).map(i => Todo(i.toString, s"test todo${i.toString}", "Test", TodoStatus.untouched, "test"))

  /**
    * GET /todo/list
    */
  def todoList() = Action { implicit req =>
    //DBから全件入手して放り込む.DBが用意できていないのでこれから
    //todos = fetchTodo()

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
  
  def view(id: String) = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    if(id == "new"){
      val todo_eiditing = Todo("new", "", "", TodoStatus.untouched, "")
      Ok(views.html.todo.editor(todo_eiditing, form, vv))
    }else{
      //DBを叩く
      //fetch(id)
      
      //DBが無いので動作確認用
      todos.find(_.id == id) match{
        case Some(todo) => {
          val filledForm = form.fill(TodoFormData(todo.title, todo.content, todo.category))
          Ok(views.html.todo.editor(todo, filledForm, vv))
        }
        case None        => NotFound(views.html.error.page404())
      }
    }
  }

  /**
    * POST /todo/:id
    */
  def store(id: String) = Action { implicit request: Request[AnyContent] =>
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
          case Some(todo) => BadRequest(views.html.todo.editor(todo, form, vv))
          case None        => NotFound(views.html.error.page404())
        }
      },

      // 処理が成功した場合に呼び出される関数
      (todoFormData: TodoFormData) => {
        var todo_id = id
        if(todo_id == "new"){
          //id生成. 下記は仮
          todo_id = (todos.length + 1).toString

          val todo = Todo(todo_id, todoFormData.title, todoFormData.content, TodoStatus.untouched, todoFormData.category)
          
          todos = todos :+ todo
          Redirect("/todo/list")
        }else{
          val todo = Todo(todo_id, todoFormData.title, todoFormData.content, TodoStatus.untouched, todoFormData.category)
          todos.find(_.id == id) match{
            case Some(todo_pre) => {
              todos = todos.updated(todos.indexOf(todo_pre), todo)

              Redirect("/todo/list")
            }
            case None        => NotFound(views.html.error.page404())
          }
        }
      }
    )
  }

  /**
   * 対象のデータを削除する
   */
  def delete() = Action { implicit request: Request[AnyContent] =>
    // requestから直接値を取得するサンプル
    val idOpt = request.body.asFormUrlEncoded.get("id").headOption
    // idがあり、値もあるときに削除
    todos.find(_.id == idOpt.get) match {
      case Some(todo) =>
        todos = todos.filterNot(_.id == idOpt.get)
        // 削除が完了したら一覧ページへリダイレクト
        Redirect(routes.TodoController.todoList())
      case None        =>
        NotFound(views.html.error.page404())
    }
  }
}
