package common
import java.io.FileInputStream
import java.util.Properties

import play.api.GlobalSettings
import play.Application
import play.api.Logger

import com.google.inject._

import javax.inject._
import javax.inject.{Inject, Singleton}

@Singleton
class Global @Inject() (injector: Injector) extends GlobalSettings {
  val props = new Properties()

  //override def getControllerInstance[A](controllerClass: Class[A]) = injector.getInstance(controllerClass)
  def loadSettings(filepath: String) = {
    props.load(new FileInputStream(filepath))
  }

  def getProperty(key: String) = {
    props.getProperty(key)
  }

  def onStart(app:Application) = {
    Logger.info("Globalsettings override: onStart")

    loadSettings("/etc/livestreamcreator.properties")
  }
}


/*

public class Global extends GlobalSettings {

  @Override
  public void onStart(Application app) {
    Logger.info("Application has started");
  }

  @Override
  public void onStop(Application app) {
    Logger.info("Application shutdown...");
  }
}
 */
