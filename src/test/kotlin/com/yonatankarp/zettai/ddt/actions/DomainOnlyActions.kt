package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoListFetcherFromMap
import com.yonatankarp.zettai.domain.ToDoListHub
import com.yonatankarp.zettai.domain.ToDoListStore
import com.yonatankarp.zettai.domain.User

class DomainOnlyActions : ZettaiActions {

    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val store: ToDoListStore = mutableMapOf()

    private val fetcher = ToDoListFetcherFromMap(store)

    private val hub = ToDoListHub(fetcher)
    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val newList = ToDoList.build(listName, items)
        fetcher.assignListToUser(user, newList)
    }

    override fun getToDoList(user: User, listName: ListName): ToDoList? = hub.getList(user, listName)

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.addItemToList(user, listName, item)
    }

    override fun allUserLists(user: User): List<ListName> = hub.getLists(user) ?: emptyList()
}
