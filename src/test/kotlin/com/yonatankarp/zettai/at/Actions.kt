package com.yonatankarp.zettai.at

import com.yonatankarp.zettai.domain.ToDoList

typealias Step = Actions.() -> Unit

interface Actions {
    fun getToDoList(user: String, listName: String): ToDoList?
}
