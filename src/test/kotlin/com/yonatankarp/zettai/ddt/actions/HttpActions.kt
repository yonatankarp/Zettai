package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.*
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner
import com.yonatankarp.zettai.domain.*
import com.yonatankarp.zettai.ui.HtmlPage
import com.yonatankarp.zettai.ui.toIsoLocalDate
import com.yonatankarp.zettai.ui.toStatus
import com.yonatankarp.zettai.webservice.Zettai
import org.http4k.client.JettyClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.Form
import org.http4k.core.body.toBody
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HttpActions(env: String = "local") : ZettaiActions {

    override val protocol: DdtProtocol = Http(env)

    private val fetcher = ToDoListFetcherFromMap(mutableMapOf())

    private val hub = ToDoListHub(fetcher)

    private val zettaiPort = 8001 // different from the one on main
    private val server = Zettai(hub).asServer(Jetty(zettaiPort))

    private val client = JettyClient()

    override fun prepare(): DomainSetUp {
        server.start()
        return Ready
    }

    override fun tearDown(): DdtActions<DdtProtocol> = also { server.stop() }

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        val response = submitToZettai(
            todoListUrl(user, listName),
            listOf(
                "itemname" to item.description,
                "itemdue" to item.dueDate?.toString()
            )
        )

        expectThat(response.status).isEqualTo(Status.SEE_OTHER)
    }

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        fetcher.assignListToUser(
            user,
            ToDoList(ListName.fromUntrustedOrThrow(listName), items.map { ToDoItem(it) })
        )
    }

    override fun allUserLists(user: User): List<ListName> {
        val response = callZettai(Method.GET, allUserListsUrl(user))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val names = extractListNamesFromPage(html)

        return names.map { name -> ListName.fromTrusted(name) }
    }

    private fun callZettai(method: Method, path: String): Response =
        client(log(Request(method, "http://localhost:$zettaiPort/$path")))

    override fun getToDoList(user: User, listName: ListName): ToDoList? {

        val response = callZettai(Method.GET, todoListUrl(user, listName))

        if (response.status == Status.NOT_FOUND)
            return null

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return ToDoList(listName, items)
    }

    private fun HtmlPage.parse(): Document = Jsoup.parse(raw)

    private fun extractItemsFromPage(html: HtmlPage): List<ToDoItem> =
        html.parse()
            .select("tr")
            .toList()
            .filter { it.select("td").size == 3 }
            .map {
                Triple(
                    it.select("td")[0].text().orEmpty(),
                    it.select("td")[1].text().toIsoLocalDate(),
                    it.select("td")[2].text().orEmpty().toStatus()
                )
            }
            .map { (name, date, status) -> ToDoItem(name, date, status) }

    private fun extractListNamesFromPage(html: HtmlPage): List<String> =
        html.parse()
            .select("tr")
            .mapNotNull { it.select("td").firstOrNull()?.text() }

    private fun submitToZettai(path: String, webForm: Form): Response =
        client(
            log(
                Request(Method.POST, "http://localhost:$zettaiPort/$path")
                    .body(webForm.toBody())
            )
        )

    private fun todoListUrl(user: User, listName: ListName) = "todo/${user.name}/${listName.name}"

    private fun allUserListsUrl(user: User) = "todo/${user.name}"
}

// TODO: use real logger
fun <T> log(something: T): T = something.also { println("--- $something") }
