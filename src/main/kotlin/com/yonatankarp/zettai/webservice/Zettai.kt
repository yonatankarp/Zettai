package com.yonatankarp.zettai.webservice

import com.yonatankarp.zettai.domain.*
import com.yonatankarp.zettai.ui.HtmlPage
import com.yonatankarp.zettai.ui.renderListsPage
import com.yonatankarp.zettai.ui.renderPage
import com.yonatankarp.zettai.utils.andUnlessNull
import org.http4k.core.*
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class Zettai(private val hub: ZettaiHub) : HttpHandler {

    val routes = routes(
        "/todo/{user}/{list}" bind Method.GET to ::getToDoList,
        "/todo/{user}/{list}" bind Method.POST to ::addNewItem,
        "/todo/{user}" bind Method.GET to ::getAllLists,
    )

    override fun invoke(request: Request): Response = routes(request)

    /**
     * Process a ToDoList request.
     */
    val processUnlessNull = ::extractListData andUnlessNull
            ::fetchListContent andUnlessNull
            ::renderListPage andUnlessNull
            ::toResponse

    private fun getToDoList(req: Request): Response =
        processUnlessNull(req)
            ?: Response(NOT_FOUND, "Not found")

    private fun extractListData(req: Request): Pair<User, ListName>? {
        val user = req.extractUser() ?: return null
        val list = req.extractListName() ?: return null
        return user to list
    }

    private fun fetchListContent(listId: Pair<User, ListName>): Pair<User, ToDoList>? =
        hub.getList(listId.first, listId.second)?.let { listId.first to it }

    private fun renderListPage(userToList: Pair<User, ToDoList>): HtmlPage =
        renderPage(userToList.first, userToList.second)

    private fun toResponse(html: HtmlPage): Response = Response(OK).body(html.raw)

    private fun addNewItem(request: Request): Response {
        val user = request.extractUser() ?: return Response(BAD_REQUEST)
        val listName = request.extractListName() ?: return Response(BAD_REQUEST)
        val item = request.extractItem() ?: return Response(BAD_REQUEST)
        return hub.addItemToList(user, listName, item)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") } ?: Response(
            NOT_FOUND
        )
    }

    private fun getAllLists(request: Request): Response {
        val user = request.extractUser() ?: return Response(BAD_REQUEST)
        return hub.getLists(user)?.let { renderListsPage(user, it) }?.let(::toResponse) ?: Response(BAD_REQUEST)
    }

    private fun Request.extractUser(): User? = path("user").orEmpty().let(::User)
    private fun Request.extractListName(): ListName? = path("list")?.let(::ListName)
    private fun Request.extractItem(): ToDoItem? = form("itemname")?.let(::ToDoItem)
}
