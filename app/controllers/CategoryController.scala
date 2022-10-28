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
import lib.model.Category
import lib.persistence._

case class CategoryFormData(
  name: String,
  slug: String,
  color: Int
)

@Singleton
class CategoryController @Inject()(val controllerComponents: ControllerComponents/*, todoRepos: TodoRepository[slick.jdbc.JdbcProfile]*/) //TodoRepository <- constructorが見つからない???
    extends BaseController with play.api.i18n.I18nSupport {

  val todoRepos = new TodoRepository[slick.jdbc.JdbcProfile]()(onMySQL.driver)
  val categoryRepos = new CategoryRepository[slick.jdbc.JdbcProfile]()(onMySQL.driver)

  /**
    * GET /todo/list
    */
  def list() = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "Category list",
      cssSrc = Seq("main.css", "list.css"),
      jsSrc  = Seq("main.js")
    )
    
    for {
      categorys <- categoryRepos.all
    } yield Ok(views.html.todo.category.list(categorys.map(_.v), Category.colors, vv))
  }

  /**
    * GET /todo/:id
    */
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "slug" -> nonEmptyText, //英字のみの指定が必要
      "color" -> number.verifying(min(0), max(255))
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )
  
  def edit(id: Long) = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )

    for {
      category_opt <- categoryRepos.get(Category.Id(id))
      category_seq <- categoryRepos.all
    }yield {
      category_opt match {
        case Some(category) => {
          val filledForm = form.fill(CategoryFormData(category.v.name, category.v.slug, category.v.color))
          Ok(views.html.todo.category.editor(category.id, category_seq.map(_.v), filledForm, vv))
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
        category_seq <- categoryRepos.all
    } yield {
        Ok(views.html.todo.category.register(category_seq.map(_.v), form, vv))
    }
  }

  /**
    * POST /todo/:id/update
    */
  def update(id: Long) = Action async { implicit request: Request[AnyContent] =>
    form.bindFromRequest().fold(
      // 処理が失敗した場合に呼び出される関数
      // 処理失敗の例: バリデーションエラー
      (formWithErrors: Form[CategoryFormData]) => {
        val vv = ViewValueHome(
          title  = "",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js")
        )
        
        for {
            category_seq <- categoryRepos.all
        } yield {
            BadRequest(views.html.todo.category.editor(id, category_seq.map(_.v), formWithErrors, vv))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (categoryFormData: CategoryFormData) => {
        //DBのデータをupdate
        //コーナーケースの抜けができているかも?

        //変更前のtodoを入手
        val category_old = Await.ready(categoryRepos.get(Category.Id(id)), Duration.Inf) 
        category_old onComplete {
          case Success(_) => println(categoryFormData.name) //debug
          case Failure(_) => throw new java.io.IOException("Failed to fetch a data from DB")
        }

        //todoFormDataから変更情報をコピーしてDB更新
        category_old.value.get.get match {
          case Some(todo) => {
            for {
              response <- categoryRepos.update(todo.map(_.copy( name=categoryFormData.name, 
                                                            slug=categoryFormData.slug, 
                                                            color=categoryFormData.color.toShort)))
            } yield {
              response match {
                case None     => NotFound(views.html.error.page404())
                case Some(_)  => Redirect("/todo/category/list")
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
      (formWithErrors: Form[CategoryFormData]) => {
        val vv = ViewValueHome(
          title  = "",
          cssSrc = Seq("main.css"),
          jsSrc  = Seq("main.js")
        )

        for {
          categorys <- categoryRepos.all
        } yield {
          BadRequest(views.html.todo.category.register(categorys.map(_.v), formWithErrors, vv))
        }
      },

      // 処理が成功した場合に呼び出される関数
      (categoryFormData: CategoryFormData) => {
        //DBに追加
        val category_new = Category.apply(categoryFormData.name, categoryFormData.slug, color=categoryFormData.color.toShort)
        Await.ready(categoryRepos.add(category_new), Duration.Inf)

        Future.successful(Redirect("/todo/category/list"))
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
            Await.ready(categoryRepos.remove(Category.Id(id)), Duration.Inf) //<- DBの成否判定が必要?
            
            //CategoryControllerでtodoTable操作してしまってるのが良く無いかも
            for {
              todo_list <- todoRepos.all
            } yield {
              for (todo <- todo_list if todo.v.category == id) {
                todoRepos.update(todo.map(_.copy(category=6))) //category(6): noCateogry
              }
            
              Redirect("/todo/category/list")
            }
          }
          case None     => Future.successful(BadRequest("/todo/category/list"))
        }
      }
      case None      => Future.successful(BadRequest("/todo/category/list"))
    }
  }
}
