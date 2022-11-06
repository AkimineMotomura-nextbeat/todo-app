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
import play.api.data.validation._
import play.api.data.validation.Constraints._
import slick.jdbc.JdbcProfile

import model.ViewValueHome
import lib.model.Category
import lib.persistence._

case class CategoryFormData(
  name: String,
  slug: String,
  color: Category.ColorStatus
)

@Singleton
class CategoryController @Inject()(val controllerComponents: ControllerComponents, val categoryRepos: CategoryRepository[JdbcProfile], val todoRepos: TodoRepository[JdbcProfile])
    extends BaseController with play.api.i18n.I18nSupport {

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
    } yield Ok(views.html.todo.category.list(categorys.map(_.v), vv))
  }

  /**
    * GET /todo/:id
    */
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "slug" -> nonEmptyText.verifying(Constraints.pattern("[0-9a-zA-Z]+".r)), 
      "color" -> mapping(
        "color" -> number.verifying(min(0), max(255))
      )(x => Category.ColorStatus.apply(x.toShort))(x => Some(x.code.toInt))
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )
  
  def edit(id: Long) = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "",
      cssSrc = Seq("main.css", "editor.css"),
      jsSrc  = Seq("main.js")
    )

    for {
      category_seq <- categoryRepos.all
    }yield {
      val category_opt = category_seq.find(_.id == id)
      
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
      cssSrc = Seq("main.css", "editor.css"),
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
        val rtv = for {
          category_old <- categoryRepos.get(Category.Id(id))
        } yield {
          category_old match {
            case None => Future.successful(NotFound(views.html.error.page404()))
            case Some(category) => {
              for {
                response <- categoryRepos.update(category.map(_.copy( name=categoryFormData.name, 
                                                                      slug=categoryFormData.slug,
                                                                      color=categoryFormData.color)))
              } yield {
                response match {
                  case Some(_)  => Redirect("/todo/category/list")
                  case None     => NotFound(views.html.error.page404())
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
      (formWithErrors: Form[CategoryFormData]) => {
        val vv = ViewValueHome(
          title  = "",
          cssSrc = Seq("main.css", "editor.css"),
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
        val category_new = Category.apply(categoryFormData.name, categoryFormData.slug, color=categoryFormData.color)
        for {
          id <- categoryRepos.add(category_new)
        } yield {
          Redirect("/todo/category/list")
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
              old <- categoryRepos.remove(Category.Id(id))
            } yield(old)
            
            //CategoryControllerでtodoTable操作してしまってるのが良く無いかも
            //削除したカテゴリーが設定されているtodoをnoCategoryに更新
            for {
              todo_list <- todoRepos.all
            } yield {
              for (todo <- todo_list if todo.v.category == id) {
                todoRepos.update(todo.map(_.copy(category=Category.Id(6)))) //category(6): noCateogry
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
