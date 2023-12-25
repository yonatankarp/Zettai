package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.events.ToDoListState

fun interface ToDoListRetriever {
    fun retrieveByName(
        user: User,
        listName: ListName,
    ): ToDoListState?
}
