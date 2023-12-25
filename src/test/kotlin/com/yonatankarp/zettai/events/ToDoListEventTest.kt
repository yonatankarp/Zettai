package com.yonatankarp.zettai.events

import com.yonatankarp.zettai.domain.generators.randomItem
import com.yonatankarp.zettai.domain.generators.randomListName
import com.yonatankarp.zettai.domain.generators.randomUser
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant

internal class ToDoListEventTest {
    private val id = ToDoListId.mint()
    private val name = randomListName()
    private val user = randomUser()
    private val item1 = randomItem()
    private val item2 = randomItem()
    private val item3 = randomItem()

    @Test
    fun `the first event create a list`() {
        // Given
        val events = listOf(ListCreated(id, user, name))

        // When
        val list = events.fold()

        // Then
        expectThat(list).isEqualTo(ActiveToDoList(id, user, name, emptyList()))
    }

    @Test
    fun `adding and removing items to active list`() {
        // Given
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ItemRemoved(id, item2),
            )

        // When
        val list = events.fold()

        // Then
        expectThat(list)
            .isEqualTo(ActiveToDoList(id, user, name, listOf(item1, item3)))
    }

    @Test
    fun `modifying an item`() {
        // Given
        val modifiedItem2 = item2.copy(description = "new description")
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ItemModified(id, item2, modifiedItem2),
            )

        // When
        val list = events.fold()

        // Then
        expectThat(list)
            .isEqualTo(ActiveToDoList(id, user, name, listOf(item1, item3, modifiedItem2)))
    }

    @Test
    fun `putting the list on hold`() {
        // Given
        val reason = "not urgent anymore"
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ListPutOnHold(id, reason),
            )

        // When
        val list = events.fold()

        // Then
        expectThat(list).isEqualTo(OnHoldToDoList(id, user, name, listOf(item1, item2, item3), reason))
    }

    @Test
    fun `releasing a list`() {
        // Given
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ListPutOnHold(id, "not urgent anymore"),
                ListReleased(id),
            )

        // When
        val list = events.fold()

        // Then
        expectThat(list).isEqualTo(ActiveToDoList(id, user, name, listOf(item1, item2, item3)))
    }

    @Test
    fun `closing a list`() {
        // Given
        val closingTime = Instant.now()
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ListClosed(id, closingTime),
            )

        // When
        val list = events.fold()

        // Then
        expectThat(list).isEqualTo(ClosedToDoList(id, user, closingTime))
    }

    @Test
    fun `releasing a list and then closing it`() {
        // Given
        val closingTime = Instant.now()
        val events =
            listOf(
                ListCreated(id, user, name),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemAdded(id, item3),
                ListPutOnHold(id, "not urgent anymore"),
                ListReleased(id),
                ListClosed(id, closingTime),
            )

        // When
        val list = events.fold()

        // Then
        expectThat(list).isEqualTo(ClosedToDoList(id, user, closingTime))
    }
}
