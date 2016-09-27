package lib

import java.net.URI
import play.utils.UriEncoding

object ApiUtil {
  def getApiUri(path: String) = {
    URI.create(UriEncoding.encodePathSegment(s"${Config.apiUri}/$path", "UTF-8"))


//    URI.create(s"${Config.apiUri}/${UriEncoding.encodePathSegment(path, "UTF-8")}")
  }
}
