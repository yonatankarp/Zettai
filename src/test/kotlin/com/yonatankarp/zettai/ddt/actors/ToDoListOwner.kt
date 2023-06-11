package com.yonatankarp.zettai.ddt.actors

import com.ubertob.pesticide.core.DdtActor
import com.yonatankarp.zettai.ddt.actions.ZettaiActions
import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotNull

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {
    private val user = User(name)

    fun `can add #item to #listname`(itemName: String, listName: String) =
        step(itemName, listName) {
            val item = ToDoItem(itemName)
            addListItem(user, ListName(listName), item)
        }

    fun `can see #listname with #itemnames`(
        listName: String,
        expectedItems: List<String>
    ) =
        step(listName, expectedItems) {
            val list = getToDoList(user, ListName(listName))

            expectThat(list)
                .isNotNull()
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `cannot see #listname`(listName: String) =
        step(listName) {
            assertThrows<AssertionFailedError> { getToDoList(user, ListName(listName)) }
        }

    fun `starts with a list`(listName: String, listItems: List<String>) {
//        TODO("Not yet implemented")
    }

    private val Assertion.Builder<ToDoList>.itemNames
        get() = get { items.map { it.description } }
}
