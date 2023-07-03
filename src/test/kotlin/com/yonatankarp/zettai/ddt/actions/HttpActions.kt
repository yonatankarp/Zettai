package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoStatus
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.domain.ZettaiOutcome
import com.yonatankarp.zettai.domain.generators.expectSuccess
import com.yonatankarp.zettai.domain.prepareToDoListHubForTests
import com.yonatankarp.zettai.ui.HtmlPage
import com.yonatankarp.zettai.utils.asSuccess
import com.yonatankarp.zettai.utils.unlessNullOrEmpty
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HttpActions(env: String = "local") : ZettaiActions {

    override val protocol: DdtProtocol = Http(env)

    private val hub = prepareToDoListHubForTests()

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
                "itemdue" to item.dueDate?.toString(),
            ),
        )

        expectThat(response.status).isEqualTo(Status.SEE_OTHER)
    }

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val listName1 = ListName.fromTrusted(listName)
        val lists = allUserLists(user).expectSuccess()
        if (listName1 !in lists) {
            val response = submitToZettai(allUserListsUrl(user), newListForm(listName1))

            expectThat(response.status).isEqualTo(Status.SEE_OTHER) // redirect same page

            items.forEach { addListItem(user, listName1, ToDoItem(it)) }
        }
    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> {
        val response = callZettai(Method.GET, allUserListsUrl(user))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val names = extractListNamesFromPage(html)

        return names.map { name -> ListName.fromTrusted(name) }.asSuccess()
    }

    override fun createList(user: User, listName: ListName) {
        val response = submitToZettai(allUserListsUrl(user), newListForm(listName))

        expectThat(response.status).isEqualTo(Status.SEE_OTHER)
    }

    override fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>> {
        val response = callZettai(Method.GET, whatsNextUrl(user))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return items.asSuccess()
    }

    private fun newListForm(listName: ListName): Form = listOf("listname" to listName.name)

    private fun callZettai(method: Method, path: String): Response =
        client(log(Request(method, "http://localhost:$zettaiPort/$path")))

    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> {
        val response = callZettai(Method.GET, todoListUrl(user, listName))

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return ToDoList(listName, items).asSuccess()
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
                    it.select("td")[2].text().orEmpty().toStatus(),
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
                    .body(webForm.toBody()),
            ),
        )

    private fun todoListUrl(user: User, listName: ListName) = "todo/${user.name}/${listName.name}"

    private fun allUserListsUrl(user: User) = "todo/${user.name}"

    private fun whatsNextUrl(user: User) = "whatsnext/${user.name}"
}

// TODO: use real logger
fun <T> log(something: T): T = something.also { println("--- $something") }

fun String?.toIsoLocalDate(): LocalDate? =
    unlessNullOrEmpty { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }

fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)
