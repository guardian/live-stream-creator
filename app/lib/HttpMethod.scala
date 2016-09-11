package lib

case class HttpMethod(verb: String) {
  override def toString: String = verb
}

object GET extends HttpMethod("GET")

object POST extends HttpMethod("POST")

object DELETE extends HttpMethod("DELETE")
