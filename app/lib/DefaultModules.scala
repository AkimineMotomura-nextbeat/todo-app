package lib

import com.google.inject.AbstractModule
import slick.jdbc._

class DefaultModules extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[JdbcProfile]).toInstance(MySQLProfile)
  }
}
