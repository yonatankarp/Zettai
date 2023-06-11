package com.yonatankarp.zettai.domain

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
    fun addItemToList(user: User, listName: ListName, item: ToDoItem): ToDoList?
}

class ToDoListHub(val fetcher: ToDoListUpdatableFetcher) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ToDoList? = fetcher(user, listName)

    override fun addItemToList(user: User, listName: ListName, item: ToDoItem): ToDoList? =
        fetcher(user, listName)
            ?.run {
                val newList = copy(items = items.replaceItem(item))
                fetcher.assignListToUser(user, newList)
            }

    private fun List<ToDoItem>.replaceItem(item: ToDoItem): List<ToDoItem> =
        filterNot { it.description == item.description } + item
}
