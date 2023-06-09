package com.yonatankarp.zettai.webservice

import com.yonatankarp.zettai.domain.*
import com.yonatankarp.zettai.ui.HtmlPage
import com.yonatankarp.zettai.utils.andThen
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class Zettai(private val hub: ZettaiHub) : HttpHandler {

    val routes = routes(
        "/todo/{user}/{list}" bind Method.GET to ::getToDoList
    )

    override fun invoke(request: Request): Response =
        routes(request)


    /**
     * Process a ToDoList request.
     */
    val processFunction = ::extractListData andThen
            ::fetchListContent andThen
            ::renderHtml andThen
            ::createResponse

    private fun getToDoList(req: Request): Response = processFunction(req)

    private fun extractListData(req: Request): Pair<User, ListName> {
        val user = req.path("user").orEmpty()
        val list = req.path("list").orEmpty()
        return User(user) to ListName(list)
    }

    private fun fetchListContent(listId: Pair<User, ListName>): ToDoList =
        hub.getList(listId.first, listId.second)
            ?: error("List unknown")

    private fun renderHtml(list: ToDoList): HtmlPage = HtmlPage(
        """
        <html>
            <body>
                <h1>Zettai</h1>
                <h2>${list.listName.name}</h2>
                <table>
                    <tbody>${renderItems(list.items)}</tbody>
                </table>
            </body>
        </html>
        """.trimIndent()
    )

    private fun renderItems(items: List<ToDoItem>): String = items.map {
        """
        <tr><td>${it.description}</td></tr>
        """.trimIndent()
    }.joinToString("")

    private fun createResponse(html: HtmlPage): Response = Response(OK).body(html.raw)
}
