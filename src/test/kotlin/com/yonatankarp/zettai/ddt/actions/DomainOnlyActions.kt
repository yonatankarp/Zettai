package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.yonatankarp.zettai.domain.*

class DomainOnlyActions : ZettaiActions {

    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val store: ToDoListStore = mutableMapOf()

    private val fetcher = ToDoListFetcherFromMap(store)

    private val hub = ToDoListHub(fetcher)

    override fun getToDoList(user: User, listName: ListName): ToDoList? = hub.getList(user, listName)

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.addItemToList(user, listName, item)
    }
}
