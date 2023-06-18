package com.yonatankarp.zettai.commands

import com.yonatankarp.zettai.domain.InconsistentStateError
import com.yonatankarp.zettai.domain.ToDoListCommandError
import com.yonatankarp.zettai.domain.ToDoListFetcherFromMap
import com.yonatankarp.zettai.domain.generators.expectFailure
import com.yonatankarp.zettai.domain.generators.expectSuccess
import com.yonatankarp.zettai.domain.generators.randomItem
import com.yonatankarp.zettai.domain.generators.randomListName
import com.yonatankarp.zettai.domain.generators.randomUser
import com.yonatankarp.zettai.events.ListCreated
import com.yonatankarp.zettai.events.ToDoListEventStore
import com.yonatankarp.zettai.events.ToDoListEventStreamerInMemory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.single

internal class ToDoListCommandsTest {

    private val fetcher = ToDoListFetcherFromMap(mutableMapOf())
    private val streamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(streamer)
    private val handler = ToDoListCommandHandler(eventStore, fetcher)

    @Test
    fun `Add list fails if the user has already a list with same name`() {
        // Given
        val cmd = CreateToDoList(randomUser(), randomListName())

        // When
        val res = handler(cmd).expectSuccess()

        // Then
        expectThat(res.single()).isA<ListCreated>()
        eventStore(res)

        // And When
        val duplicatedRes = handler(cmd).expectFailure()

        // Then
        expectThat(duplicatedRes).isA<InconsistentStateError>()
    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(randomUser(), randomListName(), randomItem())
        val res = handler(cmd).expectFailure()
        expectThat(res).isA<ToDoListCommandError>()
    }
}
