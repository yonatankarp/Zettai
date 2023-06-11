package com.yonatankarp.zettai.webservice

import com.yonatankarp.zettai.domain.*
import com.yonatankarp.zettai.ui.HtmlPage
import com.yonatankarp.zettai.ui.renderPage
import com.yonatankarp.zettai.utils.andThen
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            ::renderListPage andThen
            ::createResponse

    private fun getToDoList(req: Request): Response = processFunction(req)

    private fun extractListData(req: Request): Pair<User, ListName> {
        val user = req.path("user").orEmpty()
        val list = req.path("list").orEmpty()
        return User(user) to ListName(list)
    }

    private fun fetchListContent(listId: Pair<User, ListName>): Pair<User, ToDoList> =
        hub.getList(listId.first, listId.second)
            ?.let { listId.first to it }
            ?: error("List unknown")

    private fun renderListPage(userToList: Pair<User, ToDoList>): HtmlPage =
        renderPage(userToList.first, userToList.second)

    private fun createResponse(html: HtmlPage): Response = Response(OK).body(html.raw)
}
