import com.chartsbot.Service
import com.chartsbot.config.ConfigPaths.ScalatraPaths
import com.chartsbot.services.scalatra.ApiPrice
import org.scalatra.LifeCycle

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle with ScalatraPaths {

  lazy val apiPrice: ApiPrice = Service.get[ApiPrice]

  override def init(context: ServletContext): Unit = {

    context.setInitParameter("org.scalatra.environment", "development")

    context.mount(apiPrice, "/prices", "PricesApi")
  }
}
