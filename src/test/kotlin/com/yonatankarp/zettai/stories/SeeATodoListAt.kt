package com.yonatankarp.zettai.stories

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.webservice.Zettai
import io.kotest.assertions.fail
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer

private const val port = 8081

@Suppress("SameParameterValue")
private fun startTheApplication(user: String, listName: String, items: List<String>) {
    val toDoList = ToDoList(ListName(listName), items.map { ToDoItem(it) })
    val lists = mapOf(User(user) to listOf(toDoList))
    val server = Zettai(lists).asServer(Jetty(port)) // port different from main
    server.start()
}

@Suppress("SameParameterValue")
private fun getToDoList(user: String, listName: String): ToDoList {
    val client = JettyClient()

    val request = Request(Method.GET, "http://localhost:$port/todo/$user/$listName")
    val response = client(request)

    return if (response.status == Status.OK) parseResponse(response.bodyString())
    else fail(response.toMessage())
}

private fun parseResponse(html: String): ToDoList {
    val nameRegex = "<h2>.*<".toRegex()
    val listName = ListName(extractListName(nameRegex, html))
    val itemsRegex = "<td>.*?<".toRegex()
    val items = itemsRegex
        .findAll(html)
        .map { ToDoItem(extractItemDesc(it)) }
        .toList()
    return ToDoList(listName, items)
}

private fun extractListName(nameRegex: Regex, html: String): String =
    nameRegex.find(html)?.value
        ?.substringAfter("<h2>")
        ?.dropLast(1)
        .orEmpty()

private fun extractItemDesc(matchResult: MatchResult): String =
    matchResult.value
        .substringAfter("<td>")
        .dropLast(1)

class SeeATodoListAt : BehaviorSpec({
    given("List owners can see their lists") {
        val user = "frank"
        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")

        startTheApplication(user, listName, foodToBuy)

        `when`("User fetch list by name") {

            val list = getToDoList(user, listName)

            then("List name is correctly fetched and all items exists") {
                list.listName.name shouldBe listName
                list.items.map { it.description } shouldBe foodToBuy
            }
        }
    }
})