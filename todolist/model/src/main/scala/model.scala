package examples.todolist

package object model {
  type AppModel = (TodoList, Tag, Option[TodoItem])
}