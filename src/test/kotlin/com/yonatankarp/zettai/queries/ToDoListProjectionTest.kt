package com.yonatankarp.zettai.queries

import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.generators.randomItem
import com.yonatankarp.zettai.domain.generators.randomListName
import com.yonatankarp.zettai.domain.generators.randomString
import com.yonatankarp.zettai.domain.generators.randomUser
import com.yonatankarp.zettai.events.EventSequence
import com.yonatankarp.zettai.events.ItemAdded
import com.yonatankarp.zettai.events.ItemModified
import com.yonatankarp.zettai.events.ItemRemoved
import com.yonatankarp.zettai.events.ListCreated
import com.yonatankarp.zettai.events.ListPutOnHold
import com.yonatankarp.zettai.events.StoredEvent
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.events.ToDoListId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

open class ToDoListProjectionTest {

    @Nested
    inner class FindAll {
        @Test
        fun `findAll returns all the lists of a user`() {
            // Given
            val user = randomUser()
            val listName1 = randomListName()
            val listName2 = randomListName()
            val events = listOf(
                ListCreated(ToDoListId.mint(), user, listName1),
                ListCreated(ToDoListId.mint(), user, listName2),
                ListCreated(ToDoListId.mint(), randomUser(), randomListName()),
            )

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findAll(user).toList())
                .isEqualTo(listOf(listName1, listName2))
        }

        @Test
        fun `findAll returns empty list if user does not have any`() {
            // Given
            val user = randomUser()
            val events = listOf<ToDoListEvent>()

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findAll(user).toList())
                .isEqualTo(emptyList())
        }
    }

    @Nested
    inner class FindList {
        @Test
        fun `findList get list with correct items`() {
            // Given
            val user = randomUser()
            val listName = randomListName()
            val id = ToDoListId.mint()
            val item1 = randomItem()
            val item2 = randomItem()
            val item3 = randomItem()
            val events = listOf(
                ListCreated(id, user, listName),
                ItemAdded(id, item1),
                ItemAdded(id, item2),
                ItemModified(id, item2, item3),
                ItemRemoved(id, item1),
            )

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findList(user, listName))
                .isEqualTo(ToDoList(listName, listOf(item3)))
        }

        @Test
        fun `findList should return null if list name does not exists`() {
            // Given
            val user = randomUser()
            val events = listOf<ToDoListEvent>()

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findList(user, randomListName()))
                .isEqualTo(null)
        }

        @Test
        fun `findList should return null if user does not have any lists`() {
            // Given
            val user = randomUser()
            val events = listOf<ToDoListEvent>()

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findList(user, randomListName()))
                .isEqualTo(null)
        }
    }

    @Nested
    inner class FindAllActiveListId {
        @Test
        fun `findAllActiveListId returns all lists that are active for a user`() {
            // Given
            val user = randomUser()
            val listName1 = randomListName()
            val listName2 = randomListName()
            val listName3 = randomListName()
            val id1 = ToDoListId.mint()
            val id2 = ToDoListId.mint()
            val id3 = ToDoListId.mint()
            val events = listOf(
                ListCreated(id1, user, listName1),
                ListCreated(id2, user, listName2),
                ListCreated(id3, user, listName3),
                ListPutOnHold(id2, randomString()),
            )

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findAllActiveListId(user).toList())
                .isEqualTo(listOf(id1, id3))
        }

        @Test
        fun `findAllActiveListId returns empty list if user does not have any`() {
            // Given
            val user = randomUser()
            val events = listOf<ToDoListEvent>()

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findAllActiveListId(user).toList())
                .isEqualTo(emptyList())
        }

        @Test
        fun `findAllActiveListId returns empty list if user does not have any active lists`() {
            // Given
            val user = randomUser()
            val listName = randomListName()
            val id = ToDoListId.mint()
            val events = listOf(
                ListCreated(id, user, listName),
                ListPutOnHold(id, randomString()),
            )

            // When
            val projection = events.buildListProjection()

            // Then
            expectThat(projection.findAllActiveListId(user).toList())
                .isEqualTo(emptyList())
        }
    }

    private fun List<ToDoListEvent>.buildListProjection(): ToDoListProjection =
        ToDoListProjection { after ->
            mapIndexed { i, e -> StoredEvent(EventSequence(after.progressive + i + 1), e) }
                .asSequence()
        }.also(ToDoListProjection::update)
}
