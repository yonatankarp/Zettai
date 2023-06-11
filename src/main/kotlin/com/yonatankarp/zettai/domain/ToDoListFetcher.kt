package com.yonatankarp.zettai.domain

typealias ToDoListFetcher = (User, ListName) -> ToDoList?

interface ToDoListUpdatableFetcher : ToDoListFetcher {
    fun assignListToUser(user: User, list: ToDoList): ToDoList?
}
