package lib.argo

trait WriteHelpers {
  def someListOrNone[T](list: List[T]) = if (list.isEmpty) None else Some(list)
}
