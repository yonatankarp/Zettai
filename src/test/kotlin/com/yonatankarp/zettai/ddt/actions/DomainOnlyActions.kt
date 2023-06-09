package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.ToDoListHub
import com.yonatankarp.zettai.domain.User

class DomainOnlyActions: ZettaiActions {

    override val protocol: DdtProtocol = DomainOnly
    override fun prepare() = Ready

    private val lists: Map<User, List<ToDoList>> = emptyMap()

    private val hub = ToDoListHub(lists)

    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        hub.getList(user, listName)
}
