package com.yonatankarp.zettai.events

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.queries.projections.RowId
import java.time.Instant
import java.util.UUID

typealias ToDoListId = EntityId

data class EntityId(val raw: UUID) {
    companion object {
        fun mint() = EntityId(UUID.randomUUID())

        fun fromRowId(rowId: RowId) = EntityId(UUID.fromString(rowId.id))
    }
}

interface EntityEvent {
    val id: EntityId
}

sealed class ToDoListEvent : EntityEvent
data class ListCreated(override val id: ToDoListId, val owner: User, val name: ListName) : ToDoListEvent()
data class ItemAdded(override val id: ToDoListId, val item: ToDoItem) : ToDoListEvent()
data class ItemRemoved(override val id: ToDoListId, val item: ToDoItem) : ToDoListEvent()
data class ItemModified(override val id: ToDoListId, val prevItem: ToDoItem, val item: ToDoItem) : ToDoListEvent()
data class ListPutOnHold(override val id: ToDoListId, val reason: String) : ToDoListEvent()
data class ListReleased(override val id: ToDoListId) : ToDoListEvent()
data class ListClosed(override val id: ToDoListId, val closedOn: Instant) : ToDoListEvent()
