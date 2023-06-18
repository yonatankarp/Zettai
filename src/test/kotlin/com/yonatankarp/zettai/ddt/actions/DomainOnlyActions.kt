package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.yonatankarp.zettai.commands.AddToDoItem
import com.yonatankarp.zettai.commands.CreateToDoList
import com.yonatankarp.zettai.commands.ToDoListCommandHandler
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoListFetcherFromMap
import com.yonatankarp.zettai.domain.ToDoListHub
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.domain.ZettaiOutcome
import com.yonatankarp.zettai.domain.generators.emptyStore
import com.yonatankarp.zettai.events.ToDoListEventStore
import com.yonatankarp.zettai.events.ToDoListEventStreamerInMemory
import strikt.api.expectThat
import strikt.assertions.hasSize

class DomainOnlyActions : ZettaiActions {

    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val fetcher = ToDoListFetcherFromMap(emptyStore())
    private val streamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(streamer)

    private val cmdHandler = ToDoListCommandHandler(eventStore, fetcher)
    private val hub = ToDoListHub(fetcher, cmdHandler, eventStore)

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)

        hub.handle(CreateToDoList(user, list))
            ?: throw RuntimeException("Failed to create list $listName")

        val created = items
            .mapNotNull { hub.handle(AddToDoItem(user, list, ToDoItem(it))) }

        expectThat(created).hasSize(items.size)
    }

    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        hub.getList(user, listName)

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.handle(AddToDoItem(user, listName, item))
    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> =
        hub.getLists(user)

    override fun createList(user: User, listName: ListName) {
        hub.handle(CreateToDoList(user, listName))
    }
}
