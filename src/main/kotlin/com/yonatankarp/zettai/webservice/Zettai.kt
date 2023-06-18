package com.yonatankarp.zettai.webservice

import com.yonatankarp.zettai.commands.AddToDoItem
import com.yonatankarp.zettai.commands.CreateToDoList
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.domain.ZettaiHub
import com.yonatankarp.zettai.ui.HtmlPage
import com.yonatankarp.zettai.ui.renderListPage
import com.yonatankarp.zettai.ui.renderListsPage
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
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
        "/todo/{user}" bind Method.POST to ::createNewList,
    )

    override fun invoke(request: Request): Response = routes(request)

    private fun getToDoList(request: Request): Response {
        val user = request.extractUser() ?: return Response(BAD_REQUEST)
        val listName = request.extractListName() ?: return Response(BAD_REQUEST)

        return hub.getList(user, listName)
            ?.let { renderListPage(user, it) }
            ?.let(::toResponse)
            ?: Response(NOT_FOUND)
    }

    private fun addNewItem(request: Request): Response {
        val user = request.extractUser() ?: return Response(BAD_REQUEST)
        val listName = request.extractListName() ?: return Response(BAD_REQUEST)
        return request.extractItem()
            ?.let { AddToDoItem(user, listName, it) }
            ?.let(hub::handle)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(NOT_FOUND)
    }

    private fun getAllLists(request: Request): Response {
        val user = request.extractUser() ?: return Response(BAD_REQUEST)
        return hub.getLists(user)
            ?.let { renderListsPage(user, it) }
            ?.let(::toResponse)
            ?: Response(BAD_REQUEST)
    }

    private fun createNewList(request: Request): Response {
        val user = request.extractUser()
            ?: return Response(BAD_REQUEST).body("missing user in path")
        val listName = request.extractListNameFromForm("listname")
            ?: return Response(BAD_REQUEST).body("missing listname in form")

        return hub.handle(CreateToDoList(user, listName))
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}") }
            ?: Response(BAD_REQUEST)
    }

    private fun Request.extractUser(): User? = path("user")?.let(::User)
    private fun Request.extractListName(): ListName? = path("list")?.let(::ListName)
    private fun Request.extractItem(): ToDoItem? = form("itemname")?.let(::ToDoItem)
    private fun Request.extractListNameFromForm(formName: String): ListName? =
        form(formName)?.let(ListName.Companion::fromUntrusted)

    private fun toResponse(html: HtmlPage): Response = Response(OK).body(html.raw)
}
