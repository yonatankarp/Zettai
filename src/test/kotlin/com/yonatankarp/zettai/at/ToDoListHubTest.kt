package com.yonatankarp.zettai.at

import com.yonatankarp.zettai.domain.ToDoListHub
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ToDoListHubTest {

    private val frank = ToDoListOwner("Frank")
    private val shoppingItems = listOf("carrots", "apples", "milk")
    private val frankList = createList("shopping", shoppingItems)

    private val lists = mapOf(frank.asUser() to listOf(frankList))

    @Test
    fun `get list by user and name`() {
        val hub = ToDoListHub(lists)

        val myList = hub.getList(frank.asUser(), frankList.listName)

        expectThat(myList).isEqualTo(frankList)
    }
}
