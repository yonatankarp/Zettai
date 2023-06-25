package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.yonatankarp.zettai.commands.AddToDoItem
import com.yonatankarp.zettai.commands.CreateToDoList
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.domain.ZettaiOutcome
import com.yonatankarp.zettai.domain.prepareToDoListHubForTests
import com.yonatankarp.zettai.utils.onFailure
import strikt.api.expectThat
import strikt.assertions.hasSize

class DomainOnlyActions : ZettaiActions {

    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val hub = prepareToDoListHubForTests()

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)

        hub.handle(CreateToDoList(user, list))
            .onFailure { throw RuntimeException("Failed to create list $listName") }

        val created = items
            .map { hub.handle(AddToDoItem(user, list, ToDoItem(it))) }

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
