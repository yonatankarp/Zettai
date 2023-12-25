package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.commands.AddToDoItem
import com.yonatankarp.zettai.commands.CreateToDoList
import com.yonatankarp.zettai.domain.generators.expectFailure
import com.yonatankarp.zettai.domain.generators.expectSuccess
import com.yonatankarp.zettai.domain.generators.randomToDoList
import com.yonatankarp.zettai.domain.generators.randomUser
import com.yonatankarp.zettai.domain.generators.toDoListsGenerator
import com.yonatankarp.zettai.domain.generators.usersGenerator
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class ToDoListHubTest {
    private val hub = prepareToDoListHubForTests()

    @Test
    fun `get list by user and name`() {
        usersGenerator().take(10).forEach { user ->

            // Given
            val lists = toDoListsGenerator().take(100).toList()

            lists.forEach { list ->
                hub.handle(CreateToDoList(user, list.listName)).expectSuccess()
                list.items.forEach {
                    hub.handle(AddToDoItem(user, list.listName, it)).expectSuccess()
                }
            }

            lists.forEach { list ->
                // When
                val myList = hub.getList(user, list.listName).expectSuccess()

                // Then
                expectThat(myList).isEqualTo(list)
            }
        }
    }

    @Test
    fun `don't get list from other users`() {
        repeat(10) {
            val firstList = randomToDoList()
            val secondList = randomToDoList()
            val firstUser = randomUser()
            val secondUser = randomUser()

            expect {
                that(hub.getList(firstUser, secondList.listName).expectFailure()).isA<InvalidRequestError>()
                that(hub.getList(secondUser, firstList.listName).expectFailure()).isA<InvalidRequestError>()
            }
        }
    }
}
