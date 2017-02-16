import lib.{WowzaIncomingStreamApi, YouTubeChannelApi}
import play.api.cache.Cache
import play.api.libs.concurrent.Akka
import play.api.mvc.WithFilters
import play.api.{Application, GlobalSettings}
import play.filters.gzip.GzipFilter

import scala.concurrent.duration._

object Global extends WithFilters(new GzipFilter) with GlobalSettings {
  override def onStart(app: Application): Unit = {
    Akka.system(app).scheduler.schedule(0.seconds, 10.minutes)(
      WowzaIncomingStreamApi.list("live").map(streams => Cache.set("streams", streams, 10.minutes))
    )

    Akka.system(app).scheduler.schedule(0.seconds, 1.hours)(
      YouTubeChannelApi.list().map(channels => Cache.set("channels", channels, 1.hours))
    )
  }
}
