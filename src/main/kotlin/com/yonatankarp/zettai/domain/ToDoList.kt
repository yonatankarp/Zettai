package com.yonatankarp.zettai.domain

import java.time.LocalDate

/**
 * A ToDoList contains a list name and a list of ToDoItems
 */
data class ToDoList(val listName: ListName, val items: List<ToDoItem>) {
    companion object {
        fun build(listName: String, items: List<String>): ToDoList =
            ToDoList(ListName.fromUntrustedOrThrow(listName), items.map { ToDoItem(it) })
    }
}

/**
 * A ToDoItem contains a description string
 */
data class ListName internal constructor(val name: String) {
    companion object {
        private val validUrlPattern = "[A-Za-z0-9-]+".toRegex()
        fun fromTrusted(name: String): ListName = ListName(name)
        fun fromUntrusted(name: String): ListName? =
            if (name.matches(validUrlPattern) && name.length in 1..40) fromTrusted(name)
            else null
        fun fromUntrustedOrThrow(name: String): ListName =
            fromUntrusted(name) ?: throw IllegalArgumentException("Invalid list name $name")
    }
}

/**
 * A ToDoItem contains a description string
 */
data class ToDoItem(
    val description: String,
    val dueDate: LocalDate? = null,
    val status: ToDoStatus = ToDoStatus.Todo
)

/**
 * The status of a given ToDoItem
 */
enum class ToDoStatus {
    Todo,
    InProgress,
    Done,
    Blocked
}
