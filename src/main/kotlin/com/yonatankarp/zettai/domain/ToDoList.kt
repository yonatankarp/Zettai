package com.yonatankarp.zettai.domain

/**
 * A ToDoList contains a list name and a list of ToDoItems
 */
data class ToDoList(val listName: ListName, val items: List<ToDoItem>)

/**
 * A ToDoItem contains a description string
 */
data class ListName(val name: String)

/**
 * A ToDoItem contains a description string
 */
data class ToDoItem(val description: String)

/**
 * The status of a given ToDoItem
 */
enum class ToDoStatus {
    Todo,
    InProgress,
    Done,
    Blocked
}
