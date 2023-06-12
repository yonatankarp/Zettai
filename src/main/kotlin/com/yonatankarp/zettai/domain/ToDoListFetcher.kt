package com.yonatankarp.zettai.domain

interface ToDoListFetcher {
    fun get(user: User, listName: ListName): ToDoList?

    fun getAll(user: User): List<ListName>?
}

interface ToDoListUpdatableFetcher : ToDoListFetcher {
    fun assignListToUser(user: User, list: ToDoList): ToDoList?
}
