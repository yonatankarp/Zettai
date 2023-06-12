package com.yonatankarp.zettai.ddt.actors

import com.ubertob.pesticide.core.DdtActor
import com.yonatankarp.zettai.ddt.actions.ZettaiActions
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.map

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {
    val user = User(name)

    fun `can add #item to #listname`(itemName: String, listName: String) =
        step(itemName, listName) {
            val item = ToDoItem(itemName)
            addListItem(user, ListName(listName), item)
        }

    fun `can see #listname with #itemnames`(
        listName: String,
        expectedItems: List<String>,
    ) =
        step(listName, expectedItems) {
            val list = getToDoList(user, ListName(listName))

            expectThat(list)
                .isNotNull()
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `cannot see #listname`(listName: String) = step(listName) {
        val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName))
        expectThat(list).isNull()
    }

    fun `cannot see any list`() = step {
        val lists = allUserLists(user)
        expectThat(lists).isEmpty()
    }

    fun `can see the lists #listNames`(expectedLists: Set<String>) =
        step(expectedLists) {
            val lists = allUserLists(user)
            expectThat(lists)
                .map(ListName::name)
                .containsExactly(expectedLists)
        }

    private val Assertion.Builder<ToDoList>.itemNames
        get() = get { items.map { it.description } }
}
