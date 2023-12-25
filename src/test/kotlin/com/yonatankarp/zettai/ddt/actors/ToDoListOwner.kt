@file:Suppress("ktlint:standard:function-naming")

package com.yonatankarp.zettai.ddt.actors

import com.ubertob.pesticide.core.DdtActor
import com.yonatankarp.zettai.ddt.actions.ZettaiActions
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.domain.generators.expectSuccess
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.doesNotContain
import strikt.assertions.isEmpty
import strikt.assertions.map

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {
    val user = User(name)

    fun `can add #item to #listname`(
        itemName: String,
        listName: String,
    ) = step(itemName, listName) {
        val item = ToDoItem(itemName)
        addListItem(user, ListName(listName), item)
    }

    fun `can see #listname with #itemnames`(
        listName: String,
        expectedItems: List<String>,
    ) = step(listName, expectedItems) {
        val list = getToDoList(user, ListName.fromUntrustedOrThrow(listName)).expectSuccess()
        expectThat(list)
            .itemNames
            .containsExactlyInAnyOrder(expectedItems)
    }

    fun `cannot see #listname`(listName: String) =
        step(listName) {
            val lists = allUserLists(user).expectSuccess()
            expectThat(lists.map { it.name }).doesNotContain(listName)
        }

    fun `cannot see any list`() =
        step {
            val lists = allUserLists(user).expectSuccess()
            expectThat(lists).isEmpty()
        }

    fun `can see the lists #listNames`(expectedLists: Set<String>) =
        step(expectedLists) {
            val lists = allUserLists(user).expectSuccess()
            expectThat(lists)
                .map(ListName::name)
                .containsExactly(expectedLists)
        }

    fun `can create a new list called #listname`(listName: String) =
        step(listName) {
            createList(user, ListName.fromUntrustedOrThrow(listName))
        }

    private val Assertion.Builder<ToDoList>.itemNames
        get() = get { items.map { it.description } }
}
