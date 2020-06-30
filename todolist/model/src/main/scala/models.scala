package examples.todolist

sealed abstract class Entity extends Product with Serializable {
  def id: Option[Int]
}

final case class Tag(name: String, id: Option[Int] = None) extends Entity

final case class TodoForm(list: TodoList, tag: Tag, items: List[TodoItem])

final case class TodoItem(
    item: String,
    todoListId: Option[Int] = None,
    completed: Boolean = false,
    id: Option[Int] = None)
    extends Entity

final case class TodoList(title: String, tagId: Option[Int], id: Option[Int] = None) extends Entity
