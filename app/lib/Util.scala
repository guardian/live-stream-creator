package lib

object ApiUtil {
  def getApiUrl(path: String): String = s"${Config.apiUri}/$path"
}
