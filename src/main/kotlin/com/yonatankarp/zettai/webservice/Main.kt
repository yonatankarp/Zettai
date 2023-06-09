package com.yonatankarp.zettai.webservice

import com.yonatankarp.zettai.domain.*
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val items = listOf("write chapter", "insert code", "draw diagrams")
    val todoList = ToDoList(ListName("book"), items.map(::ToDoItem))
    val lists = mapOf(User("uberto") to listOf(todoList))
    val hub = ToDoListHub(lists)
    Zettai(hub).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto/book")
}
