package com.yonatankarp.zettai.commands

import com.yonatankarp.zettai.domain.ToDoListFetcherFromMap
import com.yonatankarp.zettai.domain.generators.randomListName
import com.yonatankarp.zettai.domain.generators.randomUser
import com.yonatankarp.zettai.events.ListCreated
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.events.ToDoListEventStore
import com.yonatankarp.zettai.events.ToDoListEventStreamerInMemory
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

internal class ToDoListCommandsTest {

    private val fetcher = ToDoListFetcherFromMap(mutableMapOf())
    private val streamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(streamer)
    private val handler = ToDoListCommandHandler(eventStore, fetcher)
    private fun handle(cmd: ToDoListCommand): List<ToDoListEvent>? = handler(cmd)?.let(eventStore)

    @Test
    fun `CreateToDoList generate the correct event`() {
        // Given
        val cmd = CreateToDoList(randomUser(), randomListName())

        // When
        val res = handler(cmd)?.single()

        // Then
        expectThat(res).isEqualTo(ListCreated(cmd.id, cmd.user, cmd.name))
    }

    @Test
    fun `Add list fails if the user has already a list with the same name`() {
        // Given
        val cmd = CreateToDoList(randomUser(), randomListName())

        // When
        val res = handle(cmd)?.single()

        // Then
        expectThat(res).isA<ListCreated>()

        // And When

        val duplicatedRes = handle(cmd)

        // Then

        expectThat(duplicatedRes).isNull()
    }
}
