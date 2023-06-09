package com.yonatankarp.zettai.at

import com.yonatankarp.zettai.domain.*
import com.yonatankarp.zettai.webservice.Zettai
import org.http4k.client.JettyClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.fail

fun startTheApplication(hub: ToDoListHub): ApplicationForAT {
    val port = 8081 // different from main
    val server = Zettai(hub).asServer(Jetty(port))
    server.start()

    val client = ClientFilters
        .SetBaseUriFrom(Uri.of("http://localhost:$port"))
        .then(JettyClient())

    return ApplicationForAT(client, server)
}


class ApplicationForAT(private val client: HttpHandler, private val server: AutoCloseable) : Actions {

    fun runScenario(vararg steps: Step) = server.use {
        steps.onEach { step -> step(this) }
    }

    override fun getToDoList(user: String, listName: String): ToDoList {
        val response = client(Request(Method.GET, "/todo/$user/$listName"))

        return if (response.status == Status.OK) parseResponse(response.bodyString())
        else fail(response.toMessage())
    }

    private fun parseResponse(html: String): ToDoList {
        val nameRegex = "<h2>.*<".toRegex()
        val listName = ListName(extractListName(nameRegex, html))
        val itemsRegex = "<td>.*?<".toRegex()
        val items = itemsRegex.findAll(html)
            .map { ToDoItem(extractItemDesc(it)) }.toList()
        return ToDoList(listName, items)
    }

    private fun extractListName(nameRegex: Regex, html: String): String =
        nameRegex.find(html)?.value
            ?.substringAfter("<h2>")
            ?.dropLast(1)
            .orEmpty()

    private fun extractItemDesc(matchResult: MatchResult): String =
        matchResult.value.substringAfter("<td>").dropLast(1)
}
