import com.softwaremill.macwire._
import controllers._
import play.filters.HttpFiltersComponents
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n._
import play.api.routing.Router
import router.Routes

/**
 * Application loader that wires up the application dependencies using Macwire
 */
class DefaultApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = new DefaultComponents(context).application
}

class DefaultComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with I18nComponents
  with HttpFiltersComponents {

  import lib.persistence.default._

  lazy val homeController     = wire[HomeController]
  lazy val categoryController = wire[CategoryController]
  lazy val todoController     = wire[TodoController]

  lazy val router: Router = {
    val prefix: String = "/"
    wire[Routes]
  }
}
