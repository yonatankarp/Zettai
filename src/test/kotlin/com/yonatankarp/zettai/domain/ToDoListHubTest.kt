package com.yonatankarp.zettai.domain

import com.yonatankarp.zettai.commands.ToDoListCommandHandler
import com.yonatankarp.zettai.domain.generators.emptyStore
import com.yonatankarp.zettai.domain.generators.randomToDoList
import com.yonatankarp.zettai.domain.generators.randomUser
import com.yonatankarp.zettai.events.ToDoListEventStore
import com.yonatankarp.zettai.events.ToDoListEventStreamerInMemory
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ToDoListHubTest {

    private val fetcher = ToDoListFetcherFromMap(emptyStore())
    private val streamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(streamer)

    private val cmdHandler = ToDoListCommandHandler(eventStore, fetcher)
    private val hub = ToDoListHub(fetcher, cmdHandler, eventStore)

    @Test
    fun `get list by user and name`() {
        repeat(10) {
            val user = randomUser()
            val list = randomToDoList()

            fetcher.assignListToUser(user, list)

            val myList = hub.getList(user, list.listName)

            expectThat(myList).isEqualTo(list)
        }
    }

    @Test
    fun `don't get list from other users`() {
        repeat(10) {
            val firstList = randomToDoList()
            val secondList = randomToDoList()
            val firstUser = randomUser()
            val secondUser = randomUser()

            fetcher.assignListToUser(firstUser, firstList)
            fetcher.assignListToUser(secondUser, secondList)

            expect {
                that(hub.getList(firstUser, secondList.listName)).isNull()
                that(hub.getList(secondUser, firstList.listName)).isNull()
            }
        }
    }
}
