package com.yonatankarp.zettai.queries

import com.yonatankarp.zettai.domain.ListName
import com.yonatankarp.zettai.domain.ToDoItem
import com.yonatankarp.zettai.domain.ToDoList
import com.yonatankarp.zettai.domain.User
import com.yonatankarp.zettai.events.EntityId
import com.yonatankarp.zettai.events.FetchStoredEvents
import com.yonatankarp.zettai.events.ItemAdded
import com.yonatankarp.zettai.events.ItemModified
import com.yonatankarp.zettai.events.ItemRemoved
import com.yonatankarp.zettai.events.ListClosed
import com.yonatankarp.zettai.events.ListCreated
import com.yonatankarp.zettai.events.ListPutOnHold
import com.yonatankarp.zettai.events.ListReleased
import com.yonatankarp.zettai.events.ToDoListEvent
import com.yonatankarp.zettai.events.ToDoListId
import com.yonatankarp.zettai.projections.ConcurrentMapProjection
import com.yonatankarp.zettai.projections.CreateRow
import com.yonatankarp.zettai.projections.DeleteRow
import com.yonatankarp.zettai.projections.DeltaRow
import com.yonatankarp.zettai.projections.InMemoryProjection
import com.yonatankarp.zettai.projections.RowId
import com.yonatankarp.zettai.projections.UpdateRow

data class ToDoListProjectionRow(val user: User, val active: Boolean, val list: ToDoList) {
    fun addItem(item: ToDoItem): ToDoListProjectionRow = copy(list = list.copy(items = list.items + item))

    fun removeItem(item: ToDoItem): ToDoListProjectionRow = copy(list = list.copy(items = list.items - item))

    fun replaceItem(
        previousItem: ToDoItem,
        item: ToDoItem,
    ): ToDoListProjectionRow = copy(list = list.copy(items = list.items - previousItem + item))

    fun putOnHold(): ToDoListProjectionRow = copy(active = false)

    fun release(): ToDoListProjectionRow = copy(active = true)
}

class ToDoListProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ToDoListProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        eventFetcher,
        ::eventProjector,
    ) {
    fun findAll(user: User): Sequence<ListName> =
        allRows().values
            .asSequence()
            .filter { it.user == user }
            .map { it.list.listName }

    fun findList(
        user: User,
        name: ListName,
    ): ToDoList? =
        allRows().values
            .firstOrNull { it.user == user && it.list.listName == name }
            ?.list

    fun findAllActiveListId(user: User): List<EntityId> =
        allRows()
            .filter { it.value.user == user && it.value.active }
            .map { ToDoListId.fromRowId(it.key) }

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ToDoListProjectionRow>> =
            when (e) {
                is ListCreated ->
                    CreateRow(
                        e.rowId(),
                        ToDoListProjectionRow(e.owner, true, ToDoList(e.name, emptyList())),
                    )

                is ItemAdded -> UpdateRow(e.rowId()) { addItem(e.item) }
                is ItemRemoved -> UpdateRow(e.rowId()) { removeItem(e.item) }
                is ItemModified -> UpdateRow(e.rowId()) { replaceItem(e.prevItem, e.item) }
                is ListPutOnHold -> UpdateRow(e.rowId()) { putOnHold() }
                is ListReleased -> UpdateRow(e.rowId()) { release() }
                is ListClosed -> DeleteRow(e.rowId())
            }.toSingle()
    }
}

private fun ToDoListEvent.rowId(): RowId = RowId(id.raw.toString())

fun <T : Any> DeltaRow<T>.toSingle(): List<DeltaRow<T>> = listOf(this)
