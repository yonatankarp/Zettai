package com.yonatankarp.zettai.domain

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
}

class ToDoListHub(private val lists: Map<User, List<ToDoList>>) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ToDoList? =
        lists[user]
            ?.first { it.listName == listName }
}
