package com.yonatankarp.zettai.webservice

import com.yonatankarp.zettai.commands.AddToDoItem
import com.yonatankarp.zettai.commands.CreateToDoList
import com.yonatankarp.zettai.commands.ToDoListCommandHandler
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoListHub
import com.yonatankarp.zettai.domain.ToDoStatus
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.events.ToDoListEventStore
import com.yonatankarp.zettai.events.ToDoListEventStreamerInMemory
import com.yonatankarp.zettai.projections.ToDoListQueryRunner
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.time.LocalDate

fun main() {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore)
    val queryRunner = ToDoListQueryRunner(streamer::fetchAfter)
    val hub = ToDoListHub(queryRunner, cmdHandler, eventStore)

//    val fetcher = ToDoListFetcherFromMap(mutableMapOf())
//    val streamer = ToDoListEventStreamerInMemory()
//    val eventStore = ToDoListEventStore(streamer)
//
//    val commandHandler = ToDoListCommandHandler(eventStore, fetcher)
//    val hub = ToDoListHub(fetcher, commandHandler, eventStore)

    hub.withExampleToDoList().withExampleItems()

    Zettai(hub).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto/book")
}

private fun ToDoListHub.withExampleToDoList(): ToDoListHub =
    also { handle(CreateToDoList(User("uberto"), ListName("book"))) }

private fun ToDoListHub.withExampleItems(): ToDoListHub =
    also { exampleItems.forEach { handle(it) } }

private val exampleItems = sequence {
    val user = User("uberto")
    val listName = ListName("book")
    yieldAll(
        listOf(
            AddToDoItem(
                user,
                listName,
                ToDoItem("prepare the diagram", tomorrow(), ToDoStatus.Done),
            ),
            AddToDoItem(
                user,
                listName,
                ToDoItem("rewrite explanations", dayAfterTomorrow(), ToDoStatus.InProgress),
            ),
            AddToDoItem(user, listName, ToDoItem("finish the chapter")),
            AddToDoItem(user, listName, ToDoItem("draft next chapter")),
        ),
    )
}

private fun tomorrow(): LocalDate = LocalDate.now().plusDays(1)
private fun dayAfterTomorrow(): LocalDate = LocalDate.now().plusDays(2)
