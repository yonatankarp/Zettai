package com.yonatankarp.zettai.webservice

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoListFetcherFromMap
import com.yonatankarp.zettai.domain.ToDoListHub
import com.yonatankarp.zettai.domain.User
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val listName = ListName("book")
    val items = listOf("write chapter", "insert code", "draw diagrams")
    val todoList = ToDoList(listName, items.map(::ToDoItem))
    val store = mutableMapOf(User("uberto") to mutableMapOf(listName to todoList))
    val fetcher = ToDoListFetcherFromMap(store)
    val hub = ToDoListHub(fetcher)
    Zettai(hub).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto/book")
}
