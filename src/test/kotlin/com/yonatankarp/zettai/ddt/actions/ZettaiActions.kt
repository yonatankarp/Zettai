package com.yonatankarp.zettai.ddt.actions

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.yonatankarp.zettai.ddt.actors.ToDoListOwner
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.domain.ZettaiOutcome

interface ZettaiActions : DdtActions<DdtProtocol> {
    fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>)
    fun ToDoListOwner.`starts with some lists`(lists: Map<String, List<String>>) =
        lists.forEach { (listName, items) ->
            `starts with a list`(listName, items)
        }

    fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList>
    fun addListItem(user: User, listName: ListName, item: ToDoItem)
    fun allUserLists(user: User): ZettaiOutcome<List<ListName>>
    fun createList(user: User, listName: ListName)
    fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>>
}
